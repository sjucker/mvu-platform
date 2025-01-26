package ch.mvurdorf.platform.repertoire;

import ch.mvurdorf.platform.noten.KompositionDto;

import java.math.BigDecimal;

public record RepertoireEntryDto(Long kompositionId,
                                 String kompositionTitel,
                                 String kompositionKomponist,
                                 String kompositionArrangeur,
                                 BigDecimal number) {

    public String label() {
        return KompositionDto.getLabel(kompositionTitel, kompositionKomponist, kompositionArrangeur);
    }
}
