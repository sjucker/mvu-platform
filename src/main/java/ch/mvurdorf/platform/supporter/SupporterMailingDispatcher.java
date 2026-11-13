package ch.mvurdorf.platform.supporter;

import ch.mvurdorf.platform.config.PlatformProperties;
import ch.mvurdorf.platform.jooq.tables.daos.SupporterMailingAttachmentDao;
import ch.mvurdorf.platform.jooq.tables.daos.SupporterMailingDao;
import ch.mvurdorf.platform.jooq.tables.daos.SupporterMailingRecipientDao;
import ch.mvurdorf.platform.jooq.tables.pojos.SupporterMailingRecipient;
import ch.mvurdorf.platform.service.MailAttachment;
import ch.mvurdorf.platform.service.MailService;
import ch.mvurdorf.platform.utils.DateUtil;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ch.mvurdorf.platform.jooq.Tables.SUPPORTER;
import static ch.mvurdorf.platform.supporter.SupporterMailingChannel.EMAIL;

/**
 * Sends the emails of a mailing in the background (separate bean, so {@link Async} is not bypassed).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupporterMailingDispatcher {

    private final SupporterMailingDao supporterMailingDao;
    private final SupporterMailingAttachmentDao supporterMailingAttachmentDao;
    private final SupporterMailingRecipientDao supporterMailingRecipientDao;
    private final SupporterService supporterService;
    private final MailService mailService;
    private final PlatformProperties platformProperties;

    @Async
    public void dispatch(Long mailingId) {
        var mailing = supporterMailingDao.findOptionalById(mailingId).orElseThrow();
        var recipients = supporterMailingRecipientDao.fetchByFkSupporterMailing(mailingId).stream()
                                                     .filter(recipient -> EMAIL.name().equals(recipient.getChannel()))
                                                     .filter(recipient -> recipient.getSentAt() == null)
                                                     .toList();
        var supporters = supporterService.fetchAll(SUPPORTER.ID.in(recipients.stream().map(SupporterMailingRecipient::getFkSupporter).toList()))
                                         .stream()
                                         .collect(Collectors.toMap(SupporterDto::id, Function.identity()));

        var attachment = supporterMailingAttachmentDao.findOptionalById(mailingId)
                                                      .map(it -> new MailAttachment(it.getFileName(), it.getContentType(), it.getData()))
                                                      .orElse(null);

        log.info("sending mailing {} to {} recipients", mailingId, recipients.size());
        for (var recipient : recipients) {
            var supporter = supporters.get(recipient.getFkSupporter());
            try {
                var html = SupporterMailingTemplate.personalize(mailing.getHtml(),
                                                                supporter,
                                                                platformProperties.supporterPortalLink(supporter.uuid()));
                mailService.sendSupporterMailing(supporter.email(), mailing.getSubject(), html, attachment);
                recipient.setSentAt(DateUtil.now());
                recipient.setError(null);
            } catch (MessagingException | RuntimeException e) {
                log.error("could not send mailing %s to %s".formatted(mailingId, supporter.email()), e);
                recipient.setError(Objects.toString(e.getMessage(), e.getClass().getSimpleName()));
            }
            supporterMailingRecipientDao.update(recipient);
        }
    }
}
