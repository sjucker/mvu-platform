package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.jooq.tables.daos.NotenPdfAssignmentDao;
import ch.mvurdorf.platform.jooq.tables.daos.NotenPdfDao;
import ch.mvurdorf.platform.jooq.tables.pojos.NotenPdf;
import ch.mvurdorf.platform.jooq.tables.pojos.NotenPdfAssignment;
import ch.mvurdorf.platform.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PageExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.apache.pdfbox.pdfwriter.compress.CompressParameters.NO_COMPRESSION;

@Service
@RequiredArgsConstructor
public class NotenPdfUploadService {

    private final NotenPdfDao notenPdfDao;
    private final NotenPdfAssignmentDao notenPdfAssignmentDao;
    private final StorageService storageService;

    @Transactional
    public void upload(Long kompositionId,
                       List<CreateNotenPdfDto> notenPdfAssignments,
                       byte[] pdf) {
        try (var pdDocument = Loader.loadPDF(pdf)) {
            var numberOfPages = pdDocument.getNumberOfPages();
            var max = notenPdfAssignments.stream().mapToInt(CreateNotenPdfDto::pageTo).max().orElse(0);
            if (numberOfPages < max) {
                throw new IllegalArgumentException("PDF hat weniger Seiten als in den Zuweisungen.");
            }

            for (var notenPdfAssignment : notenPdfAssignments) {
                var notenPdf = new NotenPdf(null, kompositionId, ofNullable(notenPdfAssignment.stimmlage()).map(Enum::name).orElse(null));
                notenPdfDao.insert(notenPdf);

                var extractor = new PageExtractor(pdDocument, notenPdfAssignment.pageFrom(), notenPdfAssignment.pageTo());

                try (var out = new ByteArrayOutputStream();
                     var pages = extractor.extract()) {
                    pages.save(out, NO_COMPRESSION);
                    storageService.write(notenPdf.getId(), out.toByteArray());
                }

                for (var assignment : notenPdfAssignment.assignments()) {
                    notenPdfAssignmentDao.insert(new NotenPdfAssignment(null, notenPdf.getId(),
                                                                        assignment.instrument().name(),
                                                                        ofNullable(assignment.stimme()).map(Enum::name).orElse(null)));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Ein unerwarteter Fehler ist aufgetreten", e);
        }
    }

}
