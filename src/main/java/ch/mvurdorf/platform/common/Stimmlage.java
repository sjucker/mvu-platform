package ch.mvurdorf.platform.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum Stimmlage implements LocalizedEnum {
    B("B♭"),
    C("C"),
    ES("E♭"),
    F("F");

    private final String description;

    public static Optional<Stimmlage> of(String stimmlage) {
        if (StringUtils.isBlank(stimmlage)) {
            return Optional.empty();
        }

        return Optional.of(Stimmlage.valueOf(stimmlage));
    }

}
