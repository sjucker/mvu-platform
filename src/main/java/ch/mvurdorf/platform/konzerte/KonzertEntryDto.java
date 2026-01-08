package ch.mvurdorf.platform.konzerte;

import ch.mvurdorf.platform.noten.KompositionDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class KonzertEntryDto {
    // either placeholder or komposition details are set
    private Integer index;
    private BigDecimal marschbuchNumber;
    private String placeholder;
    private Long kompositionId;
    private String kompositionTitel;
    private String kompositionKomponist;
    private String kompositionArrangeur;
    private String kompositionAudioSample;
    private boolean zugabe;
    private String additionalInfo;

    public String titel() {
        if (kompositionId != null) {
            return KompositionDto.getLabel(kompositionTitel, kompositionKomponist, kompositionArrangeur);
        } else {
            return "- %s -".formatted(placeholder);
        }
    }

    public boolean isPlaceholderEntry() {
        return isNotBlank(placeholder);
    }
}
