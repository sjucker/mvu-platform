package ch.mvurdorf.platform.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum Stimmlage implements LocalizedEnum, ParsableEnum<Stimmlage> {
    B("B♭", "Bb"),
    C("C", "C"),
    ES("E♭", "Eb"),
    F("F", "F");

    private final String description;
    private final String externalId;

    public static Optional<Stimmlage> of(String stimmlage) {
        if (StringUtils.isBlank(stimmlage)) {
            return Optional.empty();
        }

        return Optional.of(Stimmlage.valueOf(stimmlage));
    }

    @Override
    public Optional<Stimmlage> parse(String value) {
        return Arrays.stream(values())
                     .filter(v -> StringUtils.equalsIgnoreCase(v.getExternalId(), value))
                     .findFirst();
    }
}
