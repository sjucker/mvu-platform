package ch.mvurdorf.platform.konzerte;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import static ch.mvurdorf.platform.utils.BigDecimalUtil.formatBigDecimal;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4;
import static org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA;
import static org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA_BOLD;
import static org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA_OBLIQUE;

@Service
public class KonzertSetlistPdfService {

    private static final float MARGIN = 50;
    private static final float NUMBER_WIDTH = 50;
    private static final Color GREY = new Color(100, 100, 100);

    private static final PDFont REGULAR = new PDType1Font(HELVETICA);
    private static final PDFont BOLD = new PDType1Font(HELVETICA_BOLD);
    private static final PDFont ITALIC = new PDType1Font(HELVETICA_OBLIQUE);

    public byte[] exportSetlist(KonzertDto konzert) {
        try (var document = new PDDocument();
             var out = new ByteArrayOutputStream()) {
            try (var writer = new Writer(document)) {
                writer.text(konzert.name(), BOLD, 20, MARGIN, Color.BLACK);
                writer.text(konzert.dateTimeAndLocation(), REGULAR, 12, MARGIN, GREY);
                writer.space(20);

                // only reserve space for the Marschbuch number if at least one entry has one
                var textX = konzert.hasMarschbuchEntry() ? MARGIN + NUMBER_WIDTH : MARGIN;
                var zugabeHeaderWritten = false;
                for (var entry : konzert.entries()) {
                    if (entry.isZugabe() && !zugabeHeaderWritten) {
                        writer.space(10);
                        writer.text("Zugaben", BOLD, 12, MARGIN, GREY);
                        writer.space(4);
                        zugabeHeaderWritten = true;
                    }
                    writeEntry(writer, entry, textX);
                }
            }
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeEntry(Writer writer, KonzertEntryDto entry, float textX) throws IOException {
        if (entry.isPlaceholderEntry()) {
            writer.space(4);
            writer.text("– %s –".formatted(entry.getPlaceholder()), ITALIC, 12, textX, GREY);
            writer.space(8);
            return;
        }

        writer.ensureSpace(14 * 1.4f + 10 * 1.4f);
        writer.textAtCurrentLine(formatBigDecimal(entry.getMarschbuchNumber()), BOLD, 14, MARGIN);
        writer.text(entry.getKompositionTitel(), BOLD, 14, textX, Color.BLACK);

        var details = new ArrayList<String>();
        if (isNotBlank(entry.getKompositionKomponist())) {
            details.add(entry.getKompositionKomponist());
        }
        if (isNotBlank(entry.getKompositionArrangeur())) {
            details.add("arr. " + entry.getKompositionArrangeur());
        }
        if (!details.isEmpty()) {
            writer.text(String.join(", ", details), ITALIC, 10, textX, GREY);
        }
        writer.space(8);
    }

    /**
     * Writes lines top-down, wrapping long text and adding pages as needed.
     */
    private static final class Writer implements AutoCloseable {

        private final PDDocument document;
        private PDPageContentStream stream;
        private float y;

        Writer(PDDocument document) throws IOException {
            this.document = document;
            newPage();
        }

        void space(float height) {
            y -= height;
        }

        void ensureSpace(float height) throws IOException {
            if (y - height < MARGIN) {
                newPage();
            }
        }

        /**
         * Draws the text on the line that the next {@link #text} call will use, without advancing.
         */
        void textAtCurrentLine(String text, PDFont font, float fontSize, float x) throws IOException {
            draw(sanitize(text, font), font, fontSize, x, y - fontSize, Color.BLACK);
        }

        void text(String text, PDFont font, float fontSize, float x, Color color) throws IOException {
            var lineHeight = fontSize * 1.4f;
            for (var line : wrap(sanitize(text, font), font, fontSize, A4.getWidth() - MARGIN - x)) {
                ensureSpace(lineHeight);
                draw(line, font, fontSize, x, y - fontSize, color);
                y -= lineHeight;
            }
        }

        private void draw(String text, PDFont font, float fontSize, float x, float baseline, Color color) throws IOException {
            stream.beginText();
            stream.setFont(font, fontSize);
            stream.setNonStrokingColor(color);
            stream.newLineAtOffset(x, baseline);
            stream.showText(text);
            stream.endText();
        }

        private void newPage() throws IOException {
            if (stream != null) {
                stream.close();
            }
            var page = new PDPage(A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = A4.getHeight() - MARGIN;
        }

        private static List<String> wrap(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
            var lines = new ArrayList<String>();
            var line = new StringBuilder();
            for (var word : text.split(" ")) {
                var candidate = line.isEmpty() ? word : line + " " + word;
                if (!line.isEmpty() && font.getStringWidth(candidate) / 1000 * fontSize > maxWidth) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(candidate);
                }
            }
            lines.add(line.toString());
            return lines;
        }

        /**
         * Standard 14 fonts only support WinAnsiEncoding; replace anything else so rendering never fails.
         */
        private static String sanitize(String text, PDFont font) {
            if (text == null) {
                return "";
            }
            var result = new StringBuilder();
            text.codePoints().forEach(cp -> {
                var ch = Character.isWhitespace(cp) ? " " : Character.toString(cp);
                try {
                    font.encode(ch);
                    result.append(ch);
                } catch (IllegalArgumentException | IOException _) {
                    result.append('?');
                }
            });
            return result.toString();
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }
    }
}
