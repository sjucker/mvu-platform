package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Stimme;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record NotenAssignmentDto(@NotNull Instrument instrument,
                                 @Nullable Stimme stimme) {

    public String getDescription() {
        if (stimme != null) {
            return "%s %s".formatted(stimme.getDescription(), instrument.getDescription());
        } else {
            return instrument.getDescription();
        }
    }

    public static NotenAssignmentDto of(String instrument, String stimme) {
        return new NotenAssignmentDto(Instrument.valueOf(instrument),
                                      Stimme.of(stimme).orElse(null));
    }
}
