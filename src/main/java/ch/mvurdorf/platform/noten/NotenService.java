package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Notenschluessel;
import ch.mvurdorf.platform.common.Stimme;
import ch.mvurdorf.platform.common.Stimmlage;
import ch.mvurdorf.platform.jooq.tables.daos.NotenPdfAssignmentDao;
import ch.mvurdorf.platform.jooq.tables.daos.NotenPdfDao;
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
import java.util.List;
import java.util.Set;

import static ch.mvurdorf.platform.jooq.Tables.KOMPOSITION;
import static ch.mvurdorf.platform.jooq.Tables.NOTEN_PDF;
import static ch.mvurdorf.platform.jooq.Tables.NOTEN_PDF_ASSIGNMENT;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;
import static org.apache.pdfbox.io.IOUtils.createMemoryOnlyStreamCache;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.trueCondition;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotenService {

    private final DSLContext jooqDsl;
    private final NotenPdfDao notenPdfDao;
    private final NotenPdfAssignmentDao notenPdfAssignmentDao;
    private final StorageService storageService;

    public boolean deleteNotenPdf(Long notenPdfId) {
        notenPdfDao.deleteById(notenPdfId);
        return storageService.delete(notenPdfId);
    }

    public List<NotenPdfDto> findByKomposition(Long kompositionId) {
        return jooqDsl.select(NOTEN_PDF.ID,
                              KOMPOSITION.TITEL,
                              NOTEN_PDF.STIMMLAGE,
                              NOTEN_PDF.NOTENSCHLUESSEL,
                              multiset(
                                      select(NOTEN_PDF_ASSIGNMENT.INSTRUMENT,
                                             NOTEN_PDF_ASSIGNMENT.STIMME)
                                              .from(NOTEN_PDF_ASSIGNMENT)
                                              .where(NOTEN_PDF_ASSIGNMENT.FK_NOTEN_PDF.eq(NOTEN_PDF.ID))
                              ).convertFrom(it -> it.map(Records.mapping(NotenAssignmentDto::of))))
                      .from(NOTEN_PDF)
                      .join(KOMPOSITION).on(KOMPOSITION.ID.eq(NOTEN_PDF.FK_KOMPOSITION))
                      .where(NOTEN_PDF.FK_KOMPOSITION.eq(kompositionId))
                      .fetch(it -> new NotenPdfDto(it.value1(),
                                                   it.value2(),
                                                   it.value5().stream()
                                                     .sorted(comparing(NotenAssignmentDto::instrument))
                                                     .toList(),
                                                   Stimmlage.of(it.value3()).orElse(null),
                                                   Notenschluessel.of(it.value4()).orElse(null)))
                      .stream()
                      .sorted()
                      .toList();
    }

    public byte[] exportNotenToPdf(List<Long> kompositionIds, Instrument instrument, Set<Stimme> stimme, Set<Stimmlage> stimmlage, Set<Notenschluessel> noteneschluessel) {
        var stimmeValues = stimme.stream().map(Enum::name).collect(toSet());
        var stimmlagValues = stimmlage.stream().map(Enum::name).collect(toSet());
        var noteneschluesselValues = noteneschluessel.stream().map(Enum::name).collect(toSet());

        var notenIds = jooqDsl.selectDistinct(NOTEN_PDF.ID)
                              .from(NOTEN_PDF)
                              .join(NOTEN_PDF_ASSIGNMENT).on(NOTEN_PDF_ASSIGNMENT.FK_NOTEN_PDF.eq(NOTEN_PDF.ID))
                              .where(NOTEN_PDF.FK_KOMPOSITION.in(kompositionIds),
                                     NOTEN_PDF_ASSIGNMENT.INSTRUMENT.eq(instrument.name()),
                                     stimmeValues.isEmpty() ? trueCondition() : NOTEN_PDF_ASSIGNMENT.STIMME.isNull().or(NOTEN_PDF_ASSIGNMENT.STIMME.in(stimmeValues)),
                                     stimmlagValues.isEmpty() ? trueCondition() : NOTEN_PDF.STIMMLAGE.isNull().or(NOTEN_PDF.STIMMLAGE.in(stimmlagValues)),
                                     noteneschluesselValues.isEmpty() ? trueCondition() : NOTEN_PDF.NOTENSCHLUESSEL.isNull().or(NOTEN_PDF.NOTENSCHLUESSEL.in(noteneschluesselValues)))
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
