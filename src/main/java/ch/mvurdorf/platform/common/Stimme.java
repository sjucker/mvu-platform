package ch.mvurdorf.platform.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum Stimme implements LocalizedEnum {
    STIMME_1("1"),
    STIMME_2("2"),
    STIMME_3("3"),
    STIMME_4("4");

    private final String description;

    public static Optional<Stimme> of(String stimme) {
        if (StringUtils.isBlank(stimme)) {
            return Optional.empty();
        }

        return Optional.of(Stimme.valueOf(stimme));
    }

    public static Optional<Stimme> parse(String value) {
        return Arrays.stream(values())
                     .filter(v -> Strings.CI.equals(v.getDescription(), value))
                     .findFirst();
    }
}
