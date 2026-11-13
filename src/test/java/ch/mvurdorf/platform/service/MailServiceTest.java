package ch.mvurdorf.platform.service;

import ch.mvurdorf.platform.config.PlatformProperties;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mock.env.MockEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MailServiceTest {

    private final List<MimeMessage> sent = new ArrayList<>();

    private final MailService mailService = new MailService(null,
                                                            new MockEnvironment().withProperty("spring.mail.username", "info@test.ch"),
                                                            new JavaMailSenderImpl() {
                                                                @Override
                                                                protected void doSend(MimeMessage[] mimeMessages, Object[] originalMessages) {
                                                                    sent.addAll(List.of(mimeMessages));
                                                                }
                                                            },
                                                            null,
                                                            new PlatformProperties("https://platform", "https://support", false, "bcc@test.ch"));

    @Test
    void supporterMailingWithInlineImagesAndAttachment() throws Exception {
        var html = """
                <html><body>
                <img height="auto" src="https://platform/images/mailing/letterhead.png" width="580">
                <img src="https://platform/images/mailing/signature-praesidentin.png">
                <img src="https://platform/images/mailing/letterhead.png">
                <img src="https://platform/images/mailing/unknown.png">
                <img src="https://other/logo.png">
                </body></html>
                """;

        mailService.sendSupporterMailing("max@test.ch", "Betreff", html, new MailAttachment("flyer.pdf", "application/pdf", new byte[]{1, 2, 3}));

        assertThat(sent).hasSize(1);
        sent.getFirst().saveChanges(); // done by the transport when sending, sets the content types
        var parts = new ArrayList<Part>();
        collect(sent.getFirst(), parts);

        var text = parts.stream().filter(part -> isType(part, "text/html")).findFirst().orElseThrow();
        assertThat((String) text.getContent()).contains("src=\"cid:image1@mvurdorf.ch\" width=\"580\"",
                                                        "src=\"cid:image2@mvurdorf.ch\"",
                                                        "src=\"https://platform/images/mailing/unknown.png\"",
                                                        "src=\"https://other/logo.png\"")
                                              .doesNotContain("https://platform/images/mailing/letterhead.png", "cid:image3");

        var inline = parts.stream().filter(part -> part instanceof MimeBodyPart bodyPart && contentId(bodyPart) != null).toList();
        assertThat(inline).as("the letterhead is used twice, but only embedded once")
                          .extracting(part -> contentId((MimeBodyPart) part))
                          .containsExactly("<image1@mvurdorf.ch>", "<image2@mvurdorf.ch>");
        assertThat(inline).allSatisfy(part -> assertThat(isType(part, "image/png")).isTrue());

        assertThat(parts).filteredOn(part -> "flyer.pdf".equals(fileName(part))).hasSize(1);
    }

    private static void collect(Part part, List<Part> parts) throws Exception {
        if (part.getContent() instanceof Multipart multipart) {
            for (var i = 0; i < multipart.getCount(); i++) {
                collect(multipart.getBodyPart(i), parts);
            }
        } else {
            parts.add(part);
        }
    }

    private static boolean isType(Part part, String type) {
        try {
            return part.isMimeType(type);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String contentId(MimeBodyPart part) {
        try {
            return part.getContentID();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String fileName(Part part) {
        try {
            return part.getFileName();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
