package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Stimme;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record CreateNotenPdfAssignmentDto(@NotNull Instrument instrument,
                                          @Nullable Stimme stimme) {
}
