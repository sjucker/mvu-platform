package ch.mvurdorf.platform.supporter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SupporterMailingTemplateTest {

    @Test
    void personalize() {
        var supporter = new SupporterDto(1L, SupporterType.GOENNER, 1000001L, "uuid-1", null, "Anna & Co", "<Muster>",
                                         null, null, null, null, null, null, null, false, false, null, List.of(), List.of());

        var html = SupporterMailingTemplate.personalize("<p>{{anrede}} {{vorname}} {{nachname}}</p><a href=\"{{portalLink}}\">{{portalLink}}</a>",
                                                        supporter,
                                                        "https://support/portal/uuid-1?a=1&b=2");

        assertThat(html).isEqualTo("<p> Anna &amp; Co &lt;Muster&gt;</p>"
                                   + "<a href=\"https://support/portal/uuid-1?a=1&amp;b=2\">https://support/portal/uuid-1?a=1&amp;b=2</a>");
    }
}
