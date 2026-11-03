package ch.mvurdorf.platform.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
public class HtmlPdfService {

    /**
     * Renders the given (jsoup-parsed) HTML document to a PDF. Only CSS 2.1 is supported, remote resources are not
     * loaded.
     */
    public byte[] toPdf(Document html) {
        try (var out = new ByteArrayOutputStream()) {
            var builder = new PdfRendererBuilder();
            builder.withW3cDocument(new W3CDom().fromJsoup(html), null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Merges the given PDFs into one document, in the given order.
     */
    public byte[] merge(List<byte[]> pdfs) {
        try (var out = new ByteArrayOutputStream()) {
            var merger = new PDFMergerUtility();
            for (var pdf : pdfs) {
                merger.addSource(new RandomAccessReadBuffer(pdf));
            }
            merger.setDestinationStream(out);
            merger.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
