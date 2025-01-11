package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Stimme;
import ch.mvurdorf.platform.common.Stimmlage;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record NotenDto(Long id,
                       @NotNull Instrument instrument,
                       @Nullable Stimme stimme,
                       @Nullable Stimmlage stimmlage) {
}
