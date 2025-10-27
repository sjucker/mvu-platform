package ch.mvurdorf.platform.konzerte;

import ch.mvurdorf.platform.noten.KompositionDto;

import java.math.BigDecimal;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

// either placeholder or komposition details are set
public record KonzertEntryDto(Integer index,
                              BigDecimal marschbuchNumber,
                              String placeholder,
                              Long kompositionId,
                              String kompositionTitel,
                              String kompositionKomponist,
                              String kompositionArrangeur,
                              String kompositionAudioSample,
                              boolean zugabe) {

    public String titel() {
        if (kompositionId != null) {
            return KompositionDto.getLabel(kompositionTitel, kompositionKomponist, kompositionArrangeur);
        } else {
            return "- %s -".formatted(placeholder);
        }
    }

    public boolean isPlaceholder() {
        return isNotBlank(placeholder);
    }
}
