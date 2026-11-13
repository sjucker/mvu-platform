package ch.mvurdorf.platform.service;

import ch.mvurdorf.platform.config.PlatformProperties;
import ch.mvurdorf.platform.jooq.tables.pojos.Supporter;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.GERMAN;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private static final Pattern IMAGE_SRC = Pattern.compile("(<img\\b[^>]*?\\bsrc=\")([^\"]*)(\")", Pattern.CASE_INSENSITIVE);

    private final SpringTemplateEngine templateEngine;
    private final Environment environment;
    private final JavaMailSender mailSender;
    private final MjmlService mjmlService;
    private final PlatformProperties platformProperties;

    @Async
    public void sendSupporterRegistrationEmail(Supporter supporter) {
        log.info("sending supporter registration mail to {}", supporter.getEmail());
        try {
            var variables = new HashMap<String, Object>();
            variables.put("anrede", supporter.getAnrede());
            variables.put("vorname", supporter.getVorname());
            variables.put("link", platformProperties.supporterPortalLink(supporter.getUuid()));

            var mjml = templateEngine.process("supporter-registration", new Context(GERMAN, variables));

            var mimeMessage = mailSender.createMimeMessage();
            var helper = setSenderReceiverMetadata(mimeMessage, supporter.getEmail(), true);
            helper.setSubject("%s%s".formatted(getSubjectPrefix(), "Support Musikverein Harmonie Urdorf"));
            helper.setText(mjmlService.render(mjml), true);

            mailSender.send(mimeMessage);
        } catch (RuntimeException | MessagingException e) {
            log.error("could not send supporter registration mail %s".formatted(supporter.getEmail()), e);
        }
    }

    @Async
    public void sendReminderEmail(String email) {
        log.info("sending reminder mail to {}", email);
        try {
            var variables = new HashMap<String, Object>();
            variables.put("link", "%s/termine".formatted(platformProperties.url()));

            var mjml = templateEngine.process("reminder-absenzen", new Context(GERMAN, variables));

            var mimeMessage = mailSender.createMimeMessage();
            var helper = setSenderReceiverMetadata(mimeMessage, email, true);
            helper.setSubject("%s%s".formatted(getSubjectPrefix(), "[MVU] Erinnerung Absenzen"));
            helper.setText(mjmlService.render(mjml), true);

            mailSender.send(mimeMessage);
        } catch (RuntimeException | MessagingException e) {
            log.error("could not send reminder mail %s".formatted(email), e);
        }
    }

    /**
     * Sends an already rendered supporter mailing synchronously (no BCC, since mailings go to many recipients).
     *
     * @param attachment optional attachment (null = none)
     * @throws MessagingException if the mail could not be sent
     */
    public void sendSupporterMailing(String email, String subject, String html, MailAttachment attachment) throws MessagingException {
        log.info("sending supporter mailing to {}", email);
        var mimeMessage = mailSender.createMimeMessage();
        var helper = setSenderReceiverMetadata(mimeMessage, email, false);
        helper.setSubject("%s%s".formatted(getSubjectPrefix(), subject));
        setHtmlWithInlineImages(helper, html);
        if (attachment != null) {
            helper.addAttachment(attachment.fileName(), new ByteArrayResource(attachment.data()), attachment.contentType());
        }
        mailSender.send(mimeMessage);
    }

    /**
     * Embeds the static images of the platform (e.g., letterhead and signatures) as inline parts referenced by
     * {@code cid:}, so they are shown without loading them from the server (data URIs are blocked by most mail clients).
     */
    private void setHtmlWithInlineImages(MimeMessageHelper helper, String html) throws MessagingException {
        var contentIds = new HashMap<String, String>();
        var images = new LinkedHashMap<String, StaticImages.Image>();
        var matcher = IMAGE_SRC.matcher(html);
        var result = new StringBuilder();
        while (matcher.find()) {
            var src = matcher.group(2);
            var contentId = contentIds.get(src);
            if (contentId == null) {
                var image = StaticImages.find(src, platformProperties.url());
                if (image.isPresent()) {
                    contentId = "image%d@mvurdorf.ch".formatted(images.size() + 1);
                    contentIds.put(src, contentId);
                    images.put(contentId, image.get());
                }
            }
            var newSrc = contentId != null ? "cid:" + contentId : src;
            matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(1) + newSrc + matcher.group(3)));
        }
        matcher.appendTail(result);

        // the text must be set before the inline parts are added
        helper.setText(result.toString(), true);
        for (var image : images.entrySet()) {
            helper.addInline(image.getKey(), new ByteArrayResource(image.getValue().data()), image.getValue().contentType());
        }
    }

    private MimeMessageHelper setSenderReceiverMetadata(MimeMessage mimeMessage, String email, boolean bcc) throws MessagingException {
        var helper = new MimeMessageHelper(mimeMessage, MULTIPART_MODE_MIXED_RELATED, UTF_8.name());
        var from = environment.getRequiredProperty("spring.mail.username");
        helper.setFrom(from);
        helper.setReplyTo(from);
        if (platformProperties.overrideRecipients()) {
            helper.setTo(platformProperties.bccMail());
            log.info("override recipient: {}", email);
        } else {
            helper.setTo(email);
            if (bcc) {
                helper.setBcc(platformProperties.bccMail());
            }
        }
        return helper;
    }

    private String getSubjectPrefix() {
        if (!environment.matchesProfiles("production")) {
            return Arrays.toString(environment.getActiveProfiles()) + " ";
        }
        return "";
    }
}
