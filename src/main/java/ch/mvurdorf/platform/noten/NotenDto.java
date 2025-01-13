package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Stimmlage;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.StringJoiner;

import static java.util.Locale.ROOT;

public record NotenDto(Long id,
                       @NotNull Instrument instrument,
                       @Nullable Stimmlage stimmlage) {

    public String description() {
        var joiner = new StringJoiner(", ");
        joiner.add(instrument.getDescription());
        if (stimmlage != null) {
            joiner.add(stimmlage.getDescription());
        }
        return joiner.toString();
    }

    public String filename() {
        var joiner = new StringJoiner("-");
        joiner.add(instrument.name().toLowerCase(ROOT));
        if (stimmlage != null) {
            joiner.add(stimmlage.name().toLowerCase(ROOT));
        }
        return joiner + ".pdf";
    }
}
