package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Stimmlage;
import ch.mvurdorf.platform.jooq.tables.daos.NotenDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Noten;
import ch.mvurdorf.platform.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static ch.mvurdorf.platform.jooq.Tables.NOTEN;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.apache.pdfbox.io.IOUtils.createMemoryOnlyStreamCache;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotenService {

    private final DSLContext jooqDsl;
    private final NotenDao notenDao;
    private final StorageService storageService;

    public void insert(Long kompositionId, NotenDto noten, byte[] file) {
        var notenPojo = new Noten(null, kompositionId,
                                  noten.instrument().name(),
                                  ofNullable(noten.stimmlage()).map(Enum::name).orElse(null));
        notenDao.insert(notenPojo);
        storageService.write(notenPojo.getId(), file);
    }

    public List<NotenDto> findByKomposition(Long kompositionId) {
        return notenDao.fetchByFkKomposition(kompositionId).stream()
                       .map(noten -> new NotenDto(noten.getId(),
                                                  Instrument.valueOf(noten.getInstrument()),
                                                  Stimmlage.of(noten.getStimmlage()).orElse(null)))
                       .sorted(Comparator.comparing(NotenDto::instrument)
                                         .thenComparing(NotenDto::stimmlage, nullsLast(naturalOrder())))
                       .toList();
    }

    public byte[] exportNotenToPdf(List<Long> kompositionIds) {
        return exportNotenToPdf(kompositionIds, Arrays.stream(Instrument.values()).map(Instrument::name).collect(toSet()));
    }

    public byte[] exportNotenToPdf(List<Long> kompositionIds, Instrument instrument) {
        return exportNotenToPdf(kompositionIds, Set.of(instrument.name()));
    }

    public byte[] exportNotenToPdf(List<Long> kompositionIds, Set<String> instruments) {
        List<Long> notenIds = jooqDsl.select(NOTEN.ID)
                                     .from(NOTEN)
                                     .where(NOTEN.FK_KOMPOSITION.in(kompositionIds),
                                            NOTEN.INSTRUMENT.in(instruments))
                                     .fetch(NOTEN.ID);

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
