package ch.mvurdorf.platform.konzerte;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KonzertSetlistPdfServiceTest {

    private final KonzertSetlistPdfService service = new KonzertSetlistPdfService();

    @Test
    void exportSetlist() throws Exception {
        var entries = new ArrayList<KonzertEntryDto>();
        entries.add(entry(0, new BigDecimal("12"), 1L, "Märchenwald", "Jürg Müller", "Zoë Arrangeur", false));
        entries.add(placeholder(1, "Pause"));
        entries.add(entry(2, null, 2L, "Titel mit Sonderzeichen ☺", null, null, false));
        entries.add(entry(3, new BigDecimal("104.5"), 3L, "Radetzky-Marsch", "Johann Strauss", null, true));
        var konzert = new KonzertDto(1L, "Jahreskonzert", LocalDate.of(2026, 11, 14), LocalTime.of(20, 0), "Zentrum Urdorf", null, null, entries);

        var pdf = service.exportSetlist(konzert);

        try (var document = Loader.loadPDF(pdf)) {
            var text = new PDFTextStripper().getText(document);
            assertThat(text).contains("Jahreskonzert", "Zentrum Urdorf", "Märchenwald", "Jürg Müller, arr. Zoë Arrangeur",
                                      "– Pause –", "Titel mit Sonderzeichen ?", "Zugaben", "12", "104.5", "Radetzky-Marsch");
        }
    }

    @Test
    void exportSetlistWrapsAndPaginatesLongLists() throws Exception {
        var entries = new ArrayList<KonzertEntryDto>();
        for (int i = 0; i < 60; i++) {
            entries.add(entry(i, null, (long) i, "Ein sehr langer Titel ".repeat(6) + i, "Komponist", null, false));
        }
        var konzert = new KonzertDto(1L, "Konzert", LocalDate.of(2026, 11, 14), LocalTime.of(20, 0), null, null, null, List.copyOf(entries));

        try (var document = Loader.loadPDF(service.exportSetlist(konzert))) {
            assertThat(document.getNumberOfPages()).isGreaterThan(1);
        }
    }

    private static KonzertEntryDto entry(int index, BigDecimal marschbuchNumber, Long kompositionId, String titel, String komponist, String arrangeur, boolean zugabe) {
        return new KonzertEntryDto(index, marschbuchNumber, null, kompositionId, titel, komponist, arrangeur, null, zugabe, null);
    }

    private static KonzertEntryDto placeholder(int index, String placeholder) {
        return new KonzertEntryDto(index, null, placeholder, null, null, null, null, null, false, null);
    }
}
