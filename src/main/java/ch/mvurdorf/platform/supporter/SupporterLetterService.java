package ch.mvurdorf.platform.supporter;

import ch.mvurdorf.platform.config.PlatformProperties;
import ch.mvurdorf.platform.service.HtmlPdfService;
import ch.mvurdorf.platform.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import static java.util.Locale.GERMAN;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Turns the rendered (MJML) HTML of a mailing into a printable A4 letter, styled like the MVU letter template: the
 * letterhead and the contact details are repeated on every page, the address is positioned for a C5/C6-5 envelope with
 * the window on the right side and the portal-button is replaced by a QR code.
 */
@Service
@RequiredArgsConstructor
public class SupporterLetterService {

    private static final String STATIC_RESOURCES = "META-INF/resources";
    private static final String LETTERHEAD = "/images/mailing/letterhead.png";

    // only CSS 2.1 (plus paged media) is supported by the PDF renderer; MJML renders inline styles, hence the !important
    private static final String LETTER_STYLE = """
            @page {
                size: A4;
                margin: 45mm 20mm 28mm 25mm;
                @top-left { content: element(letterhead); vertical-align: top; }
                @bottom-left { content: element(letterfooter); vertical-align: top; }
            }
            body { margin: 0; font-family: Arial, Helvetica, sans-serif; }
            .mailing-header, .mailing-portal { display: none !important; }
            body > div, body > div > div, .mailing-closing, .mailing-signatures { max-width: none !important; }
            body > div > div > table > tbody > tr > td { padding-left: 0 !important; padding-right: 0 !important; }
            .mailing-signatures .mj-column-per-50 { width: 50% !important; }
            .mailing-closing, .mailing-signatures, .letter-portal { page-break-inside: avoid; }
            .letter-letterhead { position: running(letterhead); padding-top: 5mm; margin-left: -14mm; }
            .letter-letterhead img { width: 190.5mm; height: 31.75mm; }
            .letter-footer { position: running(letterfooter); width: 165mm; }
            .letter-footer .mailing-footer { max-width: none !important; padding: 0 !important; }
            .letter-footer .mailing-footer td { padding: 0 !important; }
            .letter-head { position: relative; height: 40mm; }
            .letter-address { position: absolute; top: 5mm; left: 93mm; width: 70mm; font-size: 11pt; line-height: 1.35; }
            .letter-portal { margin-top: 10mm; font-size: 9pt; color: #666666; }
            .letter-portal img { width: 22mm; height: 22mm; float: left; margin-right: 4mm; }
            .letter-portal p { margin: 2mm 0 0 0; }
            """;

    private final HtmlPdfService htmlPdfService;
    private final QRCodeService qrCodeService;
    private final PlatformProperties platformProperties;

    /**
     * @param html the rendered HTML of the mailing (placeholders not yet replaced)
     */
    public byte[] letter(String html, SupporterDto supporter) {
        return htmlPdfService.toPdf(letterDocument(html, supporter));
    }

    /**
     * @param html the rendered HTML of the mailing (placeholders not yet replaced)
     * @return all letters merged into one PDF, each letter starting on a new page
     */
    public byte[] letters(String html, List<SupporterDto> supporters) {
        return htmlPdfService.merge(supporters.stream()
                                              .map(supporter -> letter(html, supporter))
                                              .toList());
    }

    Document letterDocument(String html, SupporterDto supporter) {
        var portalLink = platformProperties.supporterPortalLink(supporter.uuid());
        var document = Jsoup.parse(SupporterMailingTemplate.personalize(html, supporter, portalLink));

        // remote resources (e.g., Google Fonts) are not loaded by the PDF renderer, own images are embedded instead
        document.select("link").remove();
        document.select("style").forEach(style -> style.text(style.data().replaceAll("@import[^;]+;", "")));
        document.head().appendElement("style").text(LETTER_STYLE);
        embedImages(document);

        // running elements (letterhead and footer) must precede the content to appear on the first page
        var body = document.body();
        var footer = new Element("div").addClass("letter-footer");
        var mailingFooter = document.selectFirst(".mailing-footer");
        if (mailingFooter != null) {
            footer.appendChild(mailingFooter);
        }
        body.prependChild(footer);
        body.prependElement("div").addClass("letter-letterhead")
            .appendElement("img").attr("src", staticImageDataUri(LETTERHEAD));

        var letterHead = new Element("div").addClass("letter-head");
        footer.after(letterHead);
        address(letterHead.appendElement("div").addClass("letter-address"), supporter);

        var portal = new Element("div").addClass("letter-portal");
        portal.appendElement("img").attr("src", qrCodeDataUri(portalLink));
        portal.appendElement("p").text("In deinem persönlichen Supporter-Portal kannst du deine Angaben jederzeit anpassen:");
        portal.appendElement("p").text(portalLink);
        portal.appendElement("div").attr("style", "clear: both;");
        var portalSection = document.selectFirst(".mailing-portal");
        if (portalSection != null) {
            portalSection.after(portal);
        } else {
            body.appendChild(portal);
        }

        return document;
    }

    private static void address(Element address, SupporterDto supporter) {
        address.appendElement("div").text(supporter.fullName());
        address.appendElement("div").text(supporter.getStrasseWithNr());
        address.appendElement("div").text(supporter.getPlzOrt());
        if (isNotBlank(supporter.countryCode()) && !"CH".equalsIgnoreCase(supporter.countryCode())) {
            address.appendElement("div").text(Locale.of("", supporter.countryCode()).getDisplayCountry(GERMAN));
        }
    }

    /**
     * Replaces links to static images of the platform (e.g., the signatures) by data URIs.
     */
    private void embedImages(Document document) {
        var baseUrl = platformProperties.url();
        document.select("img").forEach(img -> {
            var src = img.attr("src");
            if (src.startsWith(baseUrl + "/images/") && new ClassPathResource(STATIC_RESOURCES + src.substring(baseUrl.length())).exists()) {
                img.attr("src", staticImageDataUri(src.substring(baseUrl.length())));
            }
        });
    }

    private static String staticImageDataUri(String path) {
        try (var in = new ClassPathResource(STATIC_RESOURCES + path).getInputStream()) {
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(in.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String qrCodeDataUri(String text) {
        try (var out = new ByteArrayOutputStream()) {
            try (var imageOut = new MemoryCacheImageOutputStream(out)) {
                qrCodeService.generate(text, imageOut);
            }
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
