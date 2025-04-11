package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Notenschluessel;
import ch.mvurdorf.platform.common.Stimmlage;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateNotenPdfDto(@Nullable Stimmlage stimmlage,
                                @Nullable Notenschluessel notenschluessel,
                                @NotNull List<CreateNotenPdfAssignmentDto> assignments,
                                @NotNull int pageFrom,
                                @NotNull int pageTo) {
}
