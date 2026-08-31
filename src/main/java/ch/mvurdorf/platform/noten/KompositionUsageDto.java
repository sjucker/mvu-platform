package ch.mvurdorf.platform.noten;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything that is attached to a Komposition and gets removed together with it.
 */
public record KompositionUsageDto(int notenPdfCount,
                                  int konzertEntryCount,
                                  int repertoireEntryCount,
                                  int shareableLinkCount) {

    public boolean isEmpty() {
        return notenPdfCount == 0 && konzertEntryCount == 0 && repertoireEntryCount == 0 && shareableLinkCount == 0;
    }

    /**
     * Human readable enumeration of the attached data, e.g. {@code "3 Noten-PDFs, 1 Konzert-Programm"}.
     */
    public String description() {
        var parts = new ArrayList<String>();
        addPart(parts, notenPdfCount, "Noten-PDF", "Noten-PDFs");
        addPart(parts, konzertEntryCount, "Konzert-Programm", "Konzert-Programme");
        addPart(parts, repertoireEntryCount, "Repertoire-Eintrag", "Repertoire-Einträge");
        addPart(parts, shareableLinkCount, "Freigabe-Link", "Freigabe-Links");
        return String.join(", ", parts);
    }

    private static void addPart(List<String> parts, int count, String singular, String plural) {
        if (count > 0) {
            parts.add("%d %s".formatted(count, count == 1 ? singular : plural));
        }
    }

}
