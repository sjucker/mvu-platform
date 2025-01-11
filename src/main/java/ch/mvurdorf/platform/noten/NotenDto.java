package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Stimme;
import ch.mvurdorf.platform.common.Stimmlage;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.StringJoiner;

import static java.util.Locale.ROOT;

public record NotenDto(Long id,
                       @NotNull Instrument instrument,
                       @Nullable Stimme stimme,
                       @Nullable Stimmlage stimmlage) {

    public String description() {
        var joiner = new StringJoiner(", ");
        joiner.add(instrument.getDescription());
        if (stimme != null) {
            joiner.add(stimme.getDescription());
        }
        if (stimmlage != null) {
            joiner.add(stimmlage.getDescription());
        }
        return joiner.toString();
    }

    public String filename() {
        var joiner = new StringJoiner("-");
        joiner.add(instrument.name().toLowerCase(ROOT));
        if (stimme != null) {
            joiner.add(stimme.name().toLowerCase(ROOT));
        }
        if (stimmlage != null) {
            joiner.add(stimmlage.name().toLowerCase(ROOT));
        }
        return joiner + ".pdf";
    }
}
