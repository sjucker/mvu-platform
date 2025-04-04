package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Stimmlage;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateNotenPdfDto(@Nullable Stimmlage stimmlage,
                                @NotNull List<CreateNotenPdfAssignmentDto> assignments,
                                @NotNull int pageFrom,
                                @NotNull int pageTo) {
}
