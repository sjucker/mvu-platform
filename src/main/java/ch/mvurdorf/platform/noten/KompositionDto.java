package ch.mvurdorf.platform.noten;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public record KompositionDto(Long id,
                             String titel,
                             String komponist,
                             String arrangeur,
                             NotenFormat format,
                             String audioSample,
                             String comment) {

    public String label() {
        return getLabel(titel, komponist, arrangeur);
    }

    public static String getLabel(String titel, String komponist, String arrangeur) {
        var label = titel;
        if (isNotBlank(komponist)) {
            label = label + ", " + komponist;
        }
        if (isNotBlank(arrangeur)) {
            label = label + " (arr. " + arrangeur + ")";
        }
        return label;
    }

}
