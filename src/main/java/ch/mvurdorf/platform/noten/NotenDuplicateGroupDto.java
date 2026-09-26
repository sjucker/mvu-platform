package ch.mvurdorf.platform.noten;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record NotenDuplicateGroupDto(Long kompositionId,
                                     @NotNull List<NotenPdfDto> notenPdfs) {

    public String kompositionTitel() {
        return notenPdfs.getFirst().kompositionTitel();
    }

    public String description() {
        return notenPdfs.getFirst().description();
    }
}
