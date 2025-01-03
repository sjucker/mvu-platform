package ch.mvurdorf.platform.konzerte;

import ch.mvurdorf.platform.noten.KompositionDto;

// either placeholder or komposition details are set
public record KonzertEntryDto(Integer index,
                              String placeholder,
                              Long kompositionId,
                              String kompositionTitel,
                              String kompositionKomponist,
                              String kompositionArrangeur) {

    public String titel() {
        if (kompositionId != null) {
            return KompositionDto.getLabel(kompositionTitel, kompositionKomponist, kompositionArrangeur);
        } else {
            return "- %s -".formatted(placeholder);
        }
    }
}
