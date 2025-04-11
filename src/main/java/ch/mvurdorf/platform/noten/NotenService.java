package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Notenschluessel;
import ch.mvurdorf.platform.common.Stimmlage;
import ch.mvurdorf.platform.jooq.tables.daos.NotenPdfAssignmentDao;
import ch.mvurdorf.platform.jooq.tables.daos.NotenPdfDao;
import ch.mvurdorf.platform.jooq.tables.pojos.NotenPdf;
import ch.mvurdorf.platform.jooq.tables.pojos.NotenPdfAssignment;
import ch.mvurdorf.platform.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jooq.DSLContext;
import org.jooq.Records;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static ch.mvurdorf.platform.jooq.Tables.NOTEN_PDF;
import static ch.mvurdorf.platform.jooq.Tables.NOTEN_PDF_ASSIGNMENT;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.apache.pdfbox.io.IOUtils.createMemoryOnlyStreamCache;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotenService {

    private final DSLContext jooqDsl;
    private final NotenPdfDao notenPdfDao;
    private final NotenPdfAssignmentDao notenPdfAssignmentDao;
    private final StorageService storageService;

    public void insert(Long kompositionId, NotenPdfDto noten, byte[] file) {
        var notenPdf = new NotenPdf(null,
                                    kompositionId,
                                    ofNullable(noten.stimmlage()).map(Enum::name).orElse(null),
                                    ofNullable(noten.notenschluessel()).map(Enum::name).orElse(null));
        notenPdfDao.insert(notenPdf);
        noten.assignments().forEach(notenAssignment ->
                                            notenPdfAssignmentDao.insert(new NotenPdfAssignment(null,
                                                                                                notenPdf.getId(),
                                                                                                notenAssignment.instrument().name(),
                                                                                                ofNullable(notenAssignment.stimme()).map(Enum::name).orElse(null))));

        storageService.write(notenPdf.getId(), file);
    }

    public List<NotenPdfDto> findByKomposition(Long kompositionId) {
        return jooqDsl.select(NOTEN_PDF.ID,
                              NOTEN_PDF.STIMMLAGE,
                              NOTEN_PDF.NOTENSCHLUESSEL,
                              multiset(
                                      select(NOTEN_PDF_ASSIGNMENT.INSTRUMENT,
                                             NOTEN_PDF_ASSIGNMENT.STIMME)
                                              .from(NOTEN_PDF_ASSIGNMENT)
                                              .where(NOTEN_PDF_ASSIGNMENT.FK_NOTEN_PDF.eq(NOTEN_PDF.ID))
                              ).convertFrom(it -> it.map(Records.mapping(NotenAssignmentDto::of))))
                      .from(NOTEN_PDF)
                      .where(NOTEN_PDF.FK_KOMPOSITION.eq(kompositionId))
                      .fetch(it -> new NotenPdfDto(it.value1(),
                                                   it.value4().stream()
                                                     .sorted(comparing(NotenAssignmentDto::instrument))
                                                     .toList(),
                                                   Stimmlage.of(it.value2()).orElse(null),
                                                   Notenschluessel.of(it.value3()).orElse(null)))
                      .stream()
                      .sorted()
                      .toList();
    }

    public byte[] exportNotenToPdf(List<Long> kompositionIds) {
        return exportNotenToPdf(kompositionIds, Arrays.stream(Instrument.values()).map(Instrument::name).collect(toSet()));
    }

    public byte[] exportNotenToPdf(List<Long> kompositionIds, Instrument instrument) {
        return exportNotenToPdf(kompositionIds, Set.of(instrument.name()));
    }

    public byte[] exportNotenToPdf(List<Long> kompositionIds, Set<String> instruments) {
        List<Long> notenIds = jooqDsl.selectDistinct(NOTEN_PDF.ID)
                                     .from(NOTEN_PDF)
                                     .join(NOTEN_PDF_ASSIGNMENT).on(NOTEN_PDF_ASSIGNMENT.FK_NOTEN_PDF.eq(NOTEN_PDF.ID))
                                     .where(NOTEN_PDF.FK_KOMPOSITION.in(kompositionIds),
                                            NOTEN_PDF_ASSIGNMENT.INSTRUMENT.in(instruments))
                                     .fetch(NOTEN_PDF.ID);

        try (var out = new ByteArrayOutputStream()) {
            var pdfMergerUtility = new PDFMergerUtility();
            pdfMergerUtility.setDestinationStream(out);

            for (var notenId : notenIds) {
                var pdf = storageService.read(notenId);
                try (var document = Loader.loadPDF(pdf)) {
                    pdfMergerUtility.addSource(getRandomAccessRead(document));
                }
            }

            pdfMergerUtility.mergeDocuments(createMemoryOnlyStreamCache());
            return out.toByteArray();
        } catch (IOException e) {
            log.error("could not export PDFs", e);
            throw new UncheckedIOException(e);
        }
    }

    private RandomAccessRead getRandomAccessRead(PDDocument document) {
        try {
            var out = new ByteArrayOutputStream();
            document.save(out);
            return new RandomAccessReadBuffer(out.toByteArray());
        } catch (IOException e) {
            log.error("error writing the PDF", e);
            throw new UncheckedIOException(e);
        }
    }

}
