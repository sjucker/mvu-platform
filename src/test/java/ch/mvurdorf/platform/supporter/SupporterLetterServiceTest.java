package ch.mvurdorf.platform.supporter;

import ch.mvurdorf.platform.config.PlatformProperties;
import ch.mvurdorf.platform.service.QRCodeService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SupporterLetterServiceTest {

    // simplified structure of the HTML rendered by MJML for templates/supporter-mailing.mjml
    private static final String HTML = """
            <!doctype html>
            <html><head>
            <style type="text/css">@import url(https://fonts.googleapis.com/css?family=Roboto); body { margin: 0; }</style>
            <link href="https://fonts.googleapis.com/css?family=Roboto" rel="stylesheet" type="text/css">
            </head>
            <body><div>
            <div class="mailing-header" style="margin:0px auto;max-width:600px;"><h1>Musikverein Harmonie Urdorf</h1></div>
            <div style="margin:0px auto;max-width:600px;"><h2>{{anrede}} {{vorname}}</h2><p>Danke!</p></div>
            <div class="mailing-signatures" style="margin:0px auto;max-width:600px;"><img class="signature" src="https://platform/images/mailing/signature-praesidentin.png"></div>
            <div class="mailing-portal" style="margin:0px auto;max-width:600px;"><a href="{{portalLink}}">Zum Supporter-Portal</a></div>
            <div class="mailing-footer" style="margin:0px auto;max-width:600px;"><p>Footer</p></div>
            </div></body></html>
            """;

    private final SupporterLetterService service = new SupporterLetterService(new QRCodeService(),
                                                                              new PlatformProperties("https://platform", "https://support", false, "bcc@test.ch"));

    @Test
    void letterDocument() {
        var document = service.letterDocument(HTML, supporter("Max", "Muster", "DE"));

        assertThat(document.select("link")).as("remote stylesheets are removed").isEmpty();
        assertThat(document.select("style").first().data()).as("remote imports are removed").doesNotContain("@import");
        assertThat(document.body().child(0).is(".letter-letterhead")).as("running letterhead precedes the content").isTrue();
        assertThat(document.select(".letter-letterhead img").attr("src")).startsWith("data:image/png;base64,");
        assertThat(document.select(".letter-footer > .mailing-footer")).as("footer is moved into the running page footer").hasSize(1);
        assertThat(document.select("img.signature").attr("src")).as("own images are embedded").startsWith("data:image/png;base64,");
        assertThat(document.select(".letter-head > .letter-address").text()).isEqualTo("Max Muster Bahnhofstrasse 1 8902 Urdorf Deutschland");
        assertThat(document.select(".letter-portal img").attr("src")).startsWith("data:image/png;base64,");
        assertThat(document.select(".letter-portal").text()).contains("https://support/portal/uuid-Max");
        assertThat(document.select("h2").text()).isEqualTo("Lieber Max");
    }

    @Test
    void letters() throws Exception {
        var pdf = service.letters(HTML, List.of(supporter("Max", "Muster", "CH"), supporter("Anna", "Beispiel", null)));

        try (var document = Loader.loadPDF(pdf)) {
            assertThat(document.getNumberOfPages()).as("one page per letter").isEqualTo(2);
            var text = new PDFTextStripper().getText(document);
            assertThat(text).contains("Max Muster", "Anna Beispiel", "8902 Urdorf", "Lieber Max", "Lieber Anna",
                                      "https://support/portal/uuid-Anna", "Footer")
                            .doesNotContain("Schweiz", "{{", "Zum Supporter-Portal");
        }
    }

    private static SupporterDto supporter(String vorname, String nachname, String countryCode) {
        return new SupporterDto(1L, SupporterType.PASSIVMITGLIED, 1000001L, "uuid-" + vorname, "Lieber", vorname, nachname,
                                "Bahnhofstrasse", "1", "8902", "Urdorf", countryCode, null, null,
                                true, false, null, List.of(), List.of());
    }
}
