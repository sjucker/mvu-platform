package ch.mvurdorf.platform.common;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public enum Stimmlage implements LocalizedEnum {
    C("C"),
    B("B"),
    ES("ES");

    private final String description;

    Stimmlage(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public static Optional<Stimmlage> of(String stimmlage) {
        if (StringUtils.isBlank(stimmlage)) {
            return Optional.empty();
        }

        return Optional.of(Stimmlage.valueOf(stimmlage));
    }

}
