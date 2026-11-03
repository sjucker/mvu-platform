package ch.mvurdorf.platform.supporter;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.springframework.web.util.HtmlUtils.htmlEscape;

/**
 * Placeholders that can be used in the MJML of a mailing. The MJML is rendered to HTML once per mailing, the
 * placeholders are then replaced per supporter in the rendered HTML.
 */
@NoArgsConstructor(access = PRIVATE)
public final class SupporterMailingTemplate {

    public static final String ANREDE = "{{anrede}}";
    public static final String VORNAME = "{{vorname}}";
    public static final String NACHNAME = "{{nachname}}";
    public static final String PORTAL_LINK = "{{portalLink}}";

    public static final String EXAMPLE_CONTENT = """
            <mj-section>
                <mj-column>
                    <mj-text>
                        <h2>{{anrede}} {{vorname}}</h2>
                        <p>Vielen Dank für deine Unterstützung!</p>
                    </mj-text>
                </mj-column>
            </mj-section>
            """;

    public static String personalize(String html, SupporterDto supporter, String portalLink) {
        return StringUtils.replaceEach(html,
                                       new String[]{ANREDE, VORNAME, NACHNAME, PORTAL_LINK},
                                       new String[]{escape(supporter.anrede()),
                                                    escape(supporter.vorname()),
                                                    escape(supporter.nachname()),
                                                    escape(portalLink)});
    }

    private static String escape(String value) {
        return htmlEscape(defaultString(value));
    }
}
