package ch.mvurdorf.platform.service;

import ch.mvurdorf.platform.Application.PlatformProperties;
import ch.mvurdorf.platform.jooq.tables.pojos.Passivmitglied;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Arrays;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.GERMAN;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final SpringTemplateEngine templateEngine;
    private final Environment environment;
    private final JavaMailSender mailSender;
    private final MjmlService mjmlService;
    private final PlatformProperties platformProperties;

    @Async
    public void sendSupporterRegistrationEmail(Passivmitglied passivmitglied) {
        log.info("sending supporter registration mail to {}", passivmitglied.getEmail());
        try {
            var variables = new HashMap<String, Object>();
            variables.put("anrede", passivmitglied.getAnrede());
            variables.put("vorname", passivmitglied.getVorname());
            variables.put("link", "%s/portal/%s".formatted(platformProperties.supportUrl(), passivmitglied.getUuid()));

            var mjml = templateEngine.process("supporter-registration", new Context(GERMAN, variables));

            var mimeMessage = mailSender.createMimeMessage();
            var helper = setSenderReceiverMetadata(mimeMessage, passivmitglied.getEmail());
            helper.setSubject("%s%s".formatted(getSubjectPrefix(), "Support Musikverein Harmonie Urdorf"));
            helper.setText(mjmlService.render(mjml), true);

            mailSender.send(mimeMessage);
        } catch (RuntimeException | MessagingException e) {
            log.error("could not send supporter registration mail %s".formatted(passivmitglied.getEmail()), e);
        }
    }

    private MimeMessageHelper setSenderReceiverMetadata(MimeMessage mimeMessage, String email) throws MessagingException {
        var helper = new MimeMessageHelper(mimeMessage, MULTIPART_MODE_MIXED_RELATED, UTF_8.name());
        var from = environment.getRequiredProperty("spring.mail.username");
        helper.setFrom(from);
        helper.setReplyTo(from);
        if (platformProperties.overrideRecipients()) {
            helper.setTo(platformProperties.bccMail());
            log.info("override recipient: {}", email);
        } else {
            helper.setTo(email);
            helper.setBcc(platformProperties.bccMail());
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
