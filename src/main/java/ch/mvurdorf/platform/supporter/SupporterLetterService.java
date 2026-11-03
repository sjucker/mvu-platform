package ch.mvurdorf.platform.supporter;

import ch.mvurdorf.platform.config.PlatformProperties;
import ch.mvurdorf.platform.service.HtmlPdfService;
import ch.mvurdorf.platform.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
 * Turns the rendered (MJML) HTML of a mailing into a printable A4 letter. The address is positioned for a C5/C6-5
 * envelope with the window on the right side, the portal-button is replaced by a QR code.
 */
@Service
@RequiredArgsConstructor
public class SupporterLetterService {

    private static final String LETTER_STYLE = """
            @page { size: A4; margin: 15mm 20mm 20mm 25mm; }
            body { margin: 0; font-family: Helvetica, Arial, sans-serif; }
            .mailing-portal { display: none !important; }
            .letter-head { position: relative; height: 85mm; }
            .letter-address { position: absolute; top: 35mm; left: 93mm; width: 70mm; font-size: 11pt; line-height: 1.35; }
            .letter-portal { max-width: 600px; margin: 8mm auto 5mm auto; font-size: 9pt; }
            .letter-portal img { width: 25mm; height: 25mm; float: left; margin-right: 5mm; }
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

        // remote resources (e.g., Google Fonts) are not loaded by the PDF renderer anyway
        document.select("link").remove();
        document.select("style").forEach(style -> style.text(style.data().replaceAll("@import[^;]+;", "")));
        document.head().appendElement("style").text(LETTER_STYLE);

        var letterHead = new Element("div").addClass("letter-head");
        var header = document.selectFirst(".mailing-header");
        if (header != null) {
            header.before(letterHead);
            letterHead.appendChild(header);
        } else {
            document.body().prependChild(letterHead);
        }
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
            document.body().appendChild(portal);
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
