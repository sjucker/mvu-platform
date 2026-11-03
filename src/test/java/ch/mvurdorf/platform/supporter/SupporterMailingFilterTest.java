package ch.mvurdorf.platform.supporter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SupporterMailingFilterTest {

    @ParameterizedTest(name = "email={0}, kommunikationEmail={1}, kommunikationPost={2}, filterEmail={3}, filterPost={4} -> {5}")
    @CsvSource(nullValues = "null", value = {
            // email available, wants email
            "max@test.ch, true,  false, true,  true,  EMAIL",
            // email available, wants post
            "max@test.ch, false, true,  true,  true,  POST",
            // email available, wants both
            "max@test.ch, true,  true,  true,  true,  EMAIL POST",
            // email available, wants nothing
            "max@test.ch, false, false, true,  true,  ''",
            // no email, wants email -> post anyway
            "null,        true,  false, true,  true,  POST",
            "'  ',        false, false, true,  true,  POST",
            // channel filters
            "max@test.ch, true,  true,  false, true,  POST",
            "max@test.ch, true,  true,  true,  false, EMAIL",
            "null,        false, true,  true,  false, ''",
    })
    void channels(String email, boolean kommunikationEmail, boolean kommunikationPost,
                  boolean filterEmail, boolean filterPost, String expected) {
        var filter = new SupporterMailingFilter(Set.of(), null, filterEmail, filterPost);
        var supporter = supporter(email, kommunikationEmail, kommunikationPost);

        var expectedChannels = EnumSet.noneOf(SupporterMailingChannel.class);
        for (var channel : expected.split(" ")) {
            if (!channel.isBlank()) {
                expectedChannels.add(SupporterMailingChannel.valueOf(channel));
            }
        }
        assertThat(filter.channels(supporter)).isEqualTo(expectedChannels);
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", value = {"null", "PASSIVMITGLIED", "GOENNER,PASSIVMITGLIED"})
    void typesRoundTrip(String types) {
        var parsed = SupporterMailingFilter.typesFromString(types);
        assertThat(new SupporterMailingFilter(parsed, null, true, true).typesAsString()).isEqualTo(types);
    }

    static SupporterDto supporter(String email, boolean kommunikationEmail, boolean kommunikationPost) {
        return new SupporterDto(1L, SupporterType.PASSIVMITGLIED, 1000001L, "uuid-1", "Lieber", "Max", "Muster",
                                "Bahnhofstrasse", "1", "8902", "Urdorf", "CH", email, null,
                                kommunikationPost, kommunikationEmail, null, List.of(), List.of());
    }
}
