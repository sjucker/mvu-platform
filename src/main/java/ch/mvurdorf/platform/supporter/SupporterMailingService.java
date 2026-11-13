package ch.mvurdorf.platform.supporter;

import ch.mvurdorf.platform.config.PlatformProperties;
import ch.mvurdorf.platform.jooq.tables.daos.SupporterMailingDao;
import ch.mvurdorf.platform.jooq.tables.daos.SupporterMailingRecipientDao;
import ch.mvurdorf.platform.jooq.tables.pojos.SupporterMailing;
import ch.mvurdorf.platform.jooq.tables.pojos.SupporterMailingRecipient;
import ch.mvurdorf.platform.jooq.tables.records.SupporterMailingRecord;
import ch.mvurdorf.platform.service.MjmlService;
import ch.mvurdorf.platform.service.MjmlService.MjmlResult;
import ch.mvurdorf.platform.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static ch.mvurdorf.platform.jooq.Tables.SUPPORTER;
import static ch.mvurdorf.platform.jooq.Tables.SUPPORTER_MAILING;
import static ch.mvurdorf.platform.jooq.Tables.SUPPORTER_MAILING_RECIPIENT;
import static ch.mvurdorf.platform.supporter.SupporterMailingChannel.EMAIL;
import static ch.mvurdorf.platform.supporter.SupporterMailingChannel.POST;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.Locale.GERMAN;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.selectCount;

@Service
@RequiredArgsConstructor
public class SupporterMailingService {

    private static final Comparator<SupporterDto> BY_NAME = comparing(SupporterDto::nachname, nullsLast(String.CASE_INSENSITIVE_ORDER))
            .thenComparing(SupporterDto::vorname, nullsLast(String.CASE_INSENSITIVE_ORDER));

    private final DSLContext jooqDsl;
    private final SupporterMailingDao supporterMailingDao;
    private final SupporterMailingRecipientDao supporterMailingRecipientDao;
    private final SupporterService supporterService;
    private final SupporterLetterService supporterLetterService;
    private final SupporterMailingDispatcher supporterMailingDispatcher;
    private final MjmlService mjmlService;
    private final SpringTemplateEngine templateEngine;
    private final PlatformProperties platformProperties;

    public List<SupporterMailingDto> findAll() {
        return fetch(SUPPORTER_MAILING.ID.isNotNull());
    }

    public Optional<SupporterMailingDto> find(Long id) {
        return fetch(SUPPORTER_MAILING.ID.eq(id)).stream().findFirst();
    }

    private List<SupporterMailingDto> fetch(Condition condition) {
        var emailCount = recipientCount(EMAIL);
        var postCount = recipientCount(POST);
        return jooqDsl.select(SUPPORTER_MAILING, emailCount, postCount)
                      .from(SUPPORTER_MAILING)
                      .where(condition)
                      .orderBy(SUPPORTER_MAILING.CREATED_AT.desc())
                      .fetch(it -> toDto(it.value1(), it.value2(), it.value3()));
    }

    private static Field<Integer> recipientCount(SupporterMailingChannel channel) {
        return field(selectCount().from(SUPPORTER_MAILING_RECIPIENT)
                                  .where(SUPPORTER_MAILING_RECIPIENT.FK_SUPPORTER_MAILING.eq(SUPPORTER_MAILING.ID))
                                  .and(SUPPORTER_MAILING_RECIPIENT.CHANNEL.eq(channel.name())));
    }

    private static SupporterMailingDto toDto(SupporterMailingRecord mailing, int emailCount, int postCount) {
        return new SupporterMailingDto(mailing.getId(),
                                       mailing.getSubject(),
                                       mailing.getMjml(),
                                       new SupporterMailingFilter(SupporterMailingFilter.typesFromString(mailing.getFilterTypes()),
                                                                  mailing.getFilterPaidWithinMonths(),
                                                                  mailing.getFilterEmail(),
                                                                  mailing.getFilterPost()),
                                       mailing.getCreatedAt(),
                                       mailing.getCreatedBy(),
                                       mailing.getUpdatedAt(),
                                       mailing.getUpdatedBy(),
                                       mailing.getSentAt(),
                                       mailing.getSentBy(),
                                       emailCount,
                                       postCount);
    }

    /**
     * Creates a new draft (if id is null) or updates an existing draft.
     *
     * @return the id of the mailing
     */
    public Long save(Long id, String subject, String mjml, SupporterMailingFilter filter, String user) {
        if (id == null) {
            var mailing = new SupporterMailing();
            apply(mailing, subject, mjml, filter);
            mailing.setCreatedAt(DateUtil.now());
            mailing.setCreatedBy(user);
            supporterMailingDao.insert(mailing);
            return mailing.getId();
        }

        var mailing = supporterMailingDao.findOptionalById(id).orElseThrow();
        if (mailing.getSentAt() != null) {
            throw new IllegalStateException("mailing %s has already been sent".formatted(id));
        }
        apply(mailing, subject, mjml, filter);
        mailing.setUpdatedAt(DateUtil.now());
        mailing.setUpdatedBy(user);
        supporterMailingDao.update(mailing);
        return id;
    }

    private static void apply(SupporterMailing mailing, String subject, String mjml, SupporterMailingFilter filter) {
        mailing.setSubject(subject);
        mailing.setMjml(mjml);
        mailing.setFilterTypes(filter.typesAsString());
        mailing.setFilterPaidWithinMonths(filter.paidWithinMonths());
        mailing.setFilterEmail(filter.email());
        mailing.setFilterPost(filter.post());
    }

    public void delete(Long id) {
        jooqDsl.deleteFrom(SUPPORTER_MAILING)
               .where(SUPPORTER_MAILING.ID.eq(id))
               .and(SUPPORTER_MAILING.SENT_AT.isNull())
               .execute();
    }

    /**
     * @return all supporters matching the filter with the channels they are reached by, sorted by name
     */
    public List<Recipient> recipients(SupporterMailingFilter filter) {
        return supporterService.fetchAll(filter.condition()).stream()
                               .sorted(BY_NAME)
                               .map(supporter -> new Recipient(supporter, filter.channels(supporter)))
                               .filter(recipient -> !recipient.channels().isEmpty())
                               .toList();
    }

    /**
     * Wraps the given content with the static header/footer and renders it to HTML (placeholders not yet replaced).
     */
    public MjmlResult render(String mjmlContent) {
        var mjml = templateEngine.process("supporter-mailing", new Context(GERMAN, Map.of("content", mjmlContent,
                                                                                          "baseUrl", platformProperties.url())));
        return mjmlService.renderWithErrors(mjml);
    }

    public String emailPreview(String html, SupporterDto supporter) {
        return SupporterMailingTemplate.personalize(html, supporter, platformProperties.supporterPortalLink(supporter.uuid()));
    }

    public byte[] letterPreview(String html, SupporterDto supporter) {
        return supporterLetterService.letter(html, supporter);
    }

    /**
     * Renders the mailing, stores its recipients, marks it as sent and sends the emails in the background.
     */
    public void send(Long id, String user) {
        var mailing = find(id).orElseThrow();
        var html = render(mailing.mjml()).html();
        var recipients = recipients(mailing.filter());

        jooqDsl.transaction(_ -> {
            var updated = jooqDsl.update(SUPPORTER_MAILING)
                                 .set(SUPPORTER_MAILING.HTML, html)
                                 .set(SUPPORTER_MAILING.SENT_AT, DateUtil.now())
                                 .set(SUPPORTER_MAILING.SENT_BY, user)
                                 .where(SUPPORTER_MAILING.ID.eq(id))
                                 .and(SUPPORTER_MAILING.SENT_AT.isNull())
                                 .execute();
            if (updated == 0) {
                throw new IllegalStateException("mailing %s has already been sent".formatted(id));
            }
            supporterMailingRecipientDao.insert(recipients.stream()
                                                          .flatMap(recipient -> recipient.channels().stream()
                                                                                         .map(channel -> new SupporterMailingRecipient(null,
                                                                                                                                       id,
                                                                                                                                       recipient.supporter().id(),
                                                                                                                                       channel.name(),
                                                                                                                                       null,
                                                                                                                                       null)))
                                                          .toList());
        });

        supporterMailingDispatcher.dispatch(id);
    }

    /**
     * @return the letters of all postal recipients of a sent mailing, merged into one PDF
     */
    public byte[] letters(Long id) {
        var mailing = supporterMailingDao.findOptionalById(id).orElseThrow();
        var supporterIds = jooqDsl.select(SUPPORTER_MAILING_RECIPIENT.FK_SUPPORTER)
                                  .from(SUPPORTER_MAILING_RECIPIENT)
                                  .where(SUPPORTER_MAILING_RECIPIENT.FK_SUPPORTER_MAILING.eq(id))
                                  .and(SUPPORTER_MAILING_RECIPIENT.CHANNEL.eq(POST.name()))
                                  .fetch(SUPPORTER_MAILING_RECIPIENT.FK_SUPPORTER);
        var supporters = supporterService.fetchAll(SUPPORTER.ID.in(supporterIds)).stream()
                                         .sorted(BY_NAME)
                                         .toList();
        return supporterLetterService.letters(mailing.getHtml(), supporters);
    }

    public List<SupporterMailingRecipientDto> sentRecipients(Long id) {
        return jooqDsl.select(SUPPORTER.ID,
                              SUPPORTER.VORNAME,
                              SUPPORTER.NACHNAME,
                              SUPPORTER.EMAIL,
                              SUPPORTER_MAILING_RECIPIENT.CHANNEL,
                              SUPPORTER_MAILING_RECIPIENT.SENT_AT,
                              SUPPORTER_MAILING_RECIPIENT.ERROR)
                      .from(SUPPORTER_MAILING_RECIPIENT)
                      .join(SUPPORTER).on(SUPPORTER.ID.eq(SUPPORTER_MAILING_RECIPIENT.FK_SUPPORTER))
                      .where(SUPPORTER_MAILING_RECIPIENT.FK_SUPPORTER_MAILING.eq(id))
                      .orderBy(SUPPORTER.NACHNAME, SUPPORTER.VORNAME, SUPPORTER_MAILING_RECIPIENT.CHANNEL)
                      .fetch(it -> new SupporterMailingRecipientDto(it.value1(),
                                                                    "%s %s".formatted(it.value2(), it.value3()),
                                                                    it.value4(),
                                                                    SupporterMailingChannel.valueOf(it.value5()),
                                                                    it.value6(),
                                                                    it.value7()));
    }

    public record Recipient(SupporterDto supporter, Set<SupporterMailingChannel> channels) {
    }
}
