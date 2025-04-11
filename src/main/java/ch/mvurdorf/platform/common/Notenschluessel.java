package ch.mvurdorf.platform.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum Notenschluessel implements LocalizedEnum {
    VIOLIN("Violinschlüssel"),
    BASS("Bassschlüssel");

    private final String description;

    public static Optional<Notenschluessel> of(String notenschluessel) {
        if (StringUtils.isBlank(notenschluessel)) {
            return Optional.empty();
        }

        return Optional.of(Notenschluessel.valueOf(notenschluessel));
    }

}
