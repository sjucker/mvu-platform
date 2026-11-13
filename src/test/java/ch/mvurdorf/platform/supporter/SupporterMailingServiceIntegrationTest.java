package ch.mvurdorf.platform.supporter;

import ch.mvurdorf.platform.config.PlatformProperties;
import ch.mvurdorf.platform.jooq.tables.daos.SupporterDao;
import ch.mvurdorf.platform.jooq.tables.daos.SupporterMailingAttachmentDao;
import ch.mvurdorf.platform.jooq.tables.daos.SupporterMailingDao;
import ch.mvurdorf.platform.jooq.tables.daos.SupporterMailingRecipientDao;
import ch.mvurdorf.platform.jooq.tables.daos.SupporterPaymentDao;
import ch.mvurdorf.platform.jooq.tables.daos.SupporterVoucherDao;
import ch.mvurdorf.platform.jooq.tables.daos.VoucherDao;
import ch.mvurdorf.platform.service.HtmlPdfService;
import ch.mvurdorf.platform.service.MailAttachment;
import ch.mvurdorf.platform.service.MjmlService;
import ch.mvurdorf.platform.service.QRCodeService;
import ch.mvurdorf.platform.testing.AbstractIntegrationTest;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static ch.mvurdorf.platform.jooq.Tables.SUPPORTER;
import static ch.mvurdorf.platform.jooq.Tables.SUPPORTER_PAYMENT;
import static ch.mvurdorf.platform.supporter.SupporterMailingChannel.EMAIL;
import static ch.mvurdorf.platform.supporter.SupporterMailingChannel.POST;
import static ch.mvurdorf.platform.supporter.SupporterType.GOENNER;
import static ch.mvurdorf.platform.supporter.SupporterType.PASSIVMITGLIED;
import static ch.mvurdorf.platform.utils.DateUtil.now;
import static ch.mvurdorf.platform.utils.DateUtil.today;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class SupporterMailingServiceIntegrationTest extends AbstractIntegrationTest {

    private final List<String> renderedMjml = new ArrayList<>();
    private final List<Long> dispatchedMailings = new ArrayList<>();
    private SupporterMailingService service;

    @BeforeEach
    void setUp() {
        var configuration = jooqDsl.configuration();
        var platformProperties = new PlatformProperties("https://platform", "https://support", true, "bcc@test.ch");
        var supporterService = new SupporterService(jooqDsl, new SupporterDao(configuration), new VoucherDao(configuration),
                                                    new SupporterPaymentDao(configuration), new SupporterVoucherDao(configuration),
                                                    null, null, null);

        var templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".mjml");
        var templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        var mjmlService = new MjmlService("app", "key", "http://localhost") {
            @Override
            public MjmlResult renderWithErrors(String mjml) {
                renderedMjml.add(mjml);
                return new MjmlResult("<html><body><div class=\"mailing-header\">Header</div><p>{{anrede}} {{vorname}}</p></body></html>", List.of());
            }
        };
        var dispatcher = new SupporterMailingDispatcher(null, null, null, null, null, null) {
            @Override
            public void dispatch(Long mailingId) {
                dispatchedMailings.add(mailingId);
            }
        };

        service = new SupporterMailingService(jooqDsl,
                                              new SupporterMailingDao(configuration),
                                              new SupporterMailingAttachmentDao(configuration),
                                              new SupporterMailingRecipientDao(configuration),
                                              supporterService,
                                              new SupporterLetterService(new HtmlPdfService(), new QRCodeService(), platformProperties),
                                              dispatcher,
                                              mjmlService,
                                              templateEngine,
                                              platformProperties);
    }

    @Test
    void recipientsByTypeAndPayment() {
        var recentPassiv = supporter(PASSIVMITGLIED, "Recent", "recent@test.ch", true, false);
        payment(recentPassiv, 3);
        var oldPassiv = supporter(PASSIVMITGLIED, "Old", "old@test.ch", true, false);
        payment(oldPassiv, 30);
        var neverPassiv = supporter(PASSIVMITGLIED, "Never", null, false, false);
        var recentGoenner = supporter(GOENNER, "Goenner", "goenner@test.ch", true, true);
        payment(recentGoenner, 1);

        assertThat(names(new SupporterMailingFilter(Set.of(), null, true, true))).containsExactly("Goenner", "Never", "Old", "Recent");
        assertThat(names(new SupporterMailingFilter(Set.of(PASSIVMITGLIED), null, true, true))).containsExactly("Never", "Old", "Recent");
        assertThat(names(new SupporterMailingFilter(Set.of(), 24, true, true))).containsExactly("Goenner", "Recent");
        assertThat(names(new SupporterMailingFilter(Set.of(PASSIVMITGLIED), 24, true, true))).containsExactly("Recent");
        assertThat(names(new SupporterMailingFilter(Set.of(), null, false, true))).containsExactly("Goenner", "Never");

        var goenner = service.recipients(new SupporterMailingFilter(Set.of(GOENNER), null, true, true)).getFirst();
        assertThat(goenner.channels()).containsExactlyInAnyOrder(EMAIL, POST);
    }

    @Test
    void saveSendAndLetters() {
        supporter(PASSIVMITGLIED, "Email", "email@test.ch", true, false);
        supporter(PASSIVMITGLIED, "Both", "both@test.ch", true, true);
        supporter(PASSIVMITGLIED, "Letter", null, false, false);

        var id = service.save(null, "Betreff", "<mj-section>Inhalt</mj-section>", SupporterMailingFilter.defaultFilter(), "tester");
        service.save(id, "Neuer Betreff", "<mj-section>Neuer Inhalt</mj-section>", SupporterMailingFilter.defaultFilter(), "tester");

        service.send(id, "tester");

        assertThat(renderedMjml.getLast()).as("content is embedded into the static template")
                                          .contains("<mj-section>Neuer Inhalt</mj-section>", "mailing-header", "mailing-footer", "{{portalLink}}",
                                                    "https://platform/images/mailing/letterhead.png");
        assertThat(dispatchedMailings).containsExactly(id);

        var mailing = service.find(id).orElseThrow();
        assertThat(mailing.isSent()).isTrue();
        assertThat(mailing.subject()).isEqualTo("Neuer Betreff");
        assertThat(mailing.emailCount()).isEqualTo(2);
        assertThat(mailing.postCount()).isEqualTo(2);
        assertThat(service.sentRecipients(id)).extracting(SupporterMailingRecipientDto::name, SupporterMailingRecipientDto::channel)
                                              .containsExactly(tuple("Max Both", EMAIL),
                                                               tuple("Max Both", POST),
                                                               tuple("Max Email", EMAIL),
                                                               tuple("Max Letter", POST));

        assertThatThrownBy(() -> service.send(id, "tester")).as("a mailing can only be sent once").isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> service.save(id, "x", "x", SupporterMailingFilter.defaultFilter(), "tester")).isInstanceOf(IllegalStateException.class);
        service.delete(id);
        assertThat(service.find(id)).as("sent mailings are not deleted").isPresent();
    }

    @Test
    void attachment() {
        var id = service.save(null, "Betreff", "<mj-section/>", SupporterMailingFilter.defaultFilter(), "tester");
        assertThat(service.find(id).orElseThrow().attachmentName()).isNull();
        assertThat(service.attachment(id)).isEmpty();

        service.updateAttachment(id, new MailAttachment("alt.pdf", "application/pdf", new byte[]{1}));
        service.updateAttachment(id, new MailAttachment("flyer.pdf", "application/pdf", new byte[]{1, 2, 3}));
        assertThat(service.find(id).orElseThrow().attachmentName()).as("the attachment is replaced").isEqualTo("flyer.pdf");
        assertThat(service.findAll()).as("one row per mailing").hasSize(1);
        assertThat(service.attachment(id)).hasValueSatisfying(attachment -> {
            assertThat(attachment.fileName()).isEqualTo("flyer.pdf");
            assertThat(attachment.contentType()).isEqualTo("application/pdf");
            assertThat(attachment.data()).containsExactly(1, 2, 3);
        });

        service.updateAttachment(id, null);
        assertThat(service.attachment(id)).as("the attachment is removed").isEmpty();

        service.updateAttachment(id, new MailAttachment("flyer.pdf", "application/pdf", new byte[]{1}));
        service.send(id, "tester");
        assertThatThrownBy(() -> service.updateAttachment(id, null)).as("sent mailings cannot be changed").isInstanceOf(IllegalStateException.class);
        assertThat(service.attachment(id)).isPresent();
    }

    @Test
    void lettersOfSentMailing() throws Exception {
        supporter(PASSIVMITGLIED, "Both", "both@test.ch", true, true);
        supporter(PASSIVMITGLIED, "Letter", null, false, false);
        supporter(PASSIVMITGLIED, "Email", "email@test.ch", true, false);
        var id = service.save(null, "Betreff", "<mj-section/>", SupporterMailingFilter.defaultFilter(), "tester");
        service.send(id, "tester");

        try (var document = Loader.loadPDF(service.letters(id))) {
            assertThat(document.getNumberOfPages()).isEqualTo(2);
            var text = new PDFTextStripper().getText(document);
            assertThat(text).contains("Max Both", "Max Letter").doesNotContain("Max Email");
        }
    }

    private List<String> names(SupporterMailingFilter filter) {
        return service.recipients(filter).stream().map(recipient -> recipient.supporter().nachname()).toList();
    }

    private Long supporter(SupporterType type, String nachname, String email, boolean kommunikationEmail, boolean kommunikationPost) {
        return jooqDsl.insertInto(SUPPORTER)
                      .set(SUPPORTER.TYPE, type.name())
                      .set(SUPPORTER.VORNAME, "Max")
                      .set(SUPPORTER.NACHNAME, nachname)
                      .set(SUPPORTER.STRASSE, "Bahnhofstrasse")
                      .set(SUPPORTER.STRASSE_NR, "1")
                      .set(SUPPORTER.PLZ, "8902")
                      .set(SUPPORTER.ORT, "Urdorf")
                      .set(SUPPORTER.EMAIL, email)
                      .set(SUPPORTER.ANREDE, "Lieber")
                      .set(SUPPORTER.KOMMUNIKATION_EMAIL, kommunikationEmail)
                      .set(SUPPORTER.KOMMUNIKATION_POST, kommunikationPost)
                      .set(SUPPORTER.REGISTERED_AT, now())
                      .set(SUPPORTER.UUID, UUID.randomUUID().toString())
                      .set(SUPPORTER.EXTERNAL_ID, (long) nachname.hashCode())
                      .returning(SUPPORTER.ID)
                      .fetchOne(SUPPORTER.ID);
    }

    private void payment(Long supporterId, int monthsAgo) {
        jooqDsl.insertInto(SUPPORTER_PAYMENT)
               .set(SUPPORTER_PAYMENT.FK_SUPPORTER, supporterId)
               .set(SUPPORTER_PAYMENT.DATUM, today().minusMonths(monthsAgo))
               .set(SUPPORTER_PAYMENT.AMOUNT, BigDecimal.TEN)
               .set(SUPPORTER_PAYMENT.CREATED_AT, now())
               .set(SUPPORTER_PAYMENT.CREATED_BY, "tester")
               .execute();
    }
}
