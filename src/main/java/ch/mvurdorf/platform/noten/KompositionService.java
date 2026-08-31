package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.jooq.tables.daos.KompositionDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Komposition;
import ch.mvurdorf.platform.service.StorageService;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

import static ch.mvurdorf.platform.jooq.Tables.KONZERT_ENTRY;
import static ch.mvurdorf.platform.jooq.Tables.NOTEN_PDF;
import static ch.mvurdorf.platform.jooq.Tables.REPERTOIRE_ENTRY;
import static ch.mvurdorf.platform.jooq.Tables.SHAREABLE_LINK_KOMPOSITION;
import static ch.mvurdorf.platform.jooq.tables.Komposition.KOMPOSITION;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static org.jooq.impl.DSL.selectCount;

@Slf4j
@Service
public class KompositionService {

    private static final String NOTEN_COUNT_FIELD = "noten_count";

    private final DSLContext jooqDsl;
    private final KompositionDao kompositionDao;
    private final StorageService storageService;

    public KompositionService(DSLContext jooqDsl, KompositionDao kompositionDao, StorageService storageService) {
        this.jooqDsl = jooqDsl;
        this.kompositionDao = kompositionDao;
        this.storageService = storageService;
    }

    public void insert(KompositionDto komposition) {
        kompositionDao.insert(new Komposition(null, komposition.titel(), komposition.komponist(), komposition.arrangeur(), komposition.format().name(), komposition.audioSample(), komposition.comment()));
    }

    public void update(KompositionDto komposition) {
        kompositionDao.update(new Komposition(komposition.id(), komposition.titel(), komposition.komponist(), komposition.arrangeur(), komposition.format().name(), komposition.audioSample(), komposition.comment()));
    }

    /**
     * Counts everything that is attached to the given Komposition and would be removed together with it.
     */
    public KompositionUsageDto findUsage(Long kompositionId) {
        return new KompositionUsageDto(
                jooqDsl.fetchCount(NOTEN_PDF, NOTEN_PDF.FK_KOMPOSITION.eq(kompositionId)),
                jooqDsl.fetchCount(KONZERT_ENTRY, KONZERT_ENTRY.FK_KOMPOSITION.eq(kompositionId)),
                jooqDsl.fetchCount(REPERTOIRE_ENTRY, REPERTOIRE_ENTRY.FK_KOMPOSITION.eq(kompositionId)),
                jooqDsl.fetchCount(SHAREABLE_LINK_KOMPOSITION, SHAREABLE_LINK_KOMPOSITION.KOMPOSITION_ID.eq(kompositionId))
        );
    }

    /**
     * Deletes the Komposition together with everything attached to it: the Noten-PDFs (including the files in the
     * storage and their instrument assignments), the Konzert-Programm entries, the Repertoire entries and the
     * Freigabe-Link entries.
     */
    @Transactional
    public void delete(Long kompositionId) {
        log.info("deleting komposition {}", kompositionId);

        var notenPdfIds = jooqDsl.select(NOTEN_PDF.ID)
                                 .from(NOTEN_PDF)
                                 .where(NOTEN_PDF.FK_KOMPOSITION.eq(kompositionId))
                                 .fetch(NOTEN_PDF.ID);

        jooqDsl.deleteFrom(SHAREABLE_LINK_KOMPOSITION)
               .where(SHAREABLE_LINK_KOMPOSITION.KOMPOSITION_ID.eq(kompositionId))
               .execute();
        jooqDsl.deleteFrom(REPERTOIRE_ENTRY)
               .where(REPERTOIRE_ENTRY.FK_KOMPOSITION.eq(kompositionId))
               .execute();
        jooqDsl.deleteFrom(KONZERT_ENTRY)
               .where(KONZERT_ENTRY.FK_KOMPOSITION.eq(kompositionId))
               .execute();

        // noten_pdf and noten_pdf_assignment are removed by the database (on delete cascade)
        kompositionDao.deleteById(kompositionId);

        notenPdfIds.forEach(notenPdfId -> {
            if (!storageService.delete(notenPdfId)) {
                log.warn("could not delete stored PDF {} of komposition {}", notenPdfId, kompositionId);
            }
        });
    }

    public List<KompositionDto> findAllSorted() {
        return kompositionDao.findAll().stream()
                             .map(komposition -> new KompositionDto(komposition.getId(), komposition.getTitel(), komposition.getKomponist(), komposition.getArrangeur(),
                                                                    NotenFormat.valueOf(komposition.getFormat()), komposition.getAudioSample(), komposition.getComment(), 0))
                             .sorted(comparing(KompositionDto::titel)
                                             .thenComparing(KompositionDto::komponist, nullsLast(naturalOrder()))
                                             .thenComparing(KompositionDto::arrangeur, nullsLast(naturalOrder())))
                             .toList();
    }

    public ConfigurableFilterDataProvider<KompositionDto, Void, String> dataProvider() {
        var dataProvider = DataProvider.<KompositionDto, String>fromFilteringCallbacks(
                query -> fetch(query.getFilter().orElse(null), query.getOffset(), query.getLimit()),
                query -> count(query.getFilter().orElse(null))
        );
        return dataProvider.withConfigurableFilter();
    }

    private Stream<KompositionDto> fetch(String filter, int offset, int limit) {
        var select = jooqDsl.select(KOMPOSITION.ID,
                                    KOMPOSITION.TITEL,
                                    KOMPOSITION.KOMPONIST,
                                    KOMPOSITION.ARRANGEUR,
                                    KOMPOSITION.FORMAT,
                                    KOMPOSITION.AUDIO_SAMPLE,
                                    KOMPOSITION.COMMENT,
                                    selectCount().from(NOTEN_PDF).where(KOMPOSITION.ID.eq(NOTEN_PDF.FK_KOMPOSITION)).asField(NOTEN_COUNT_FIELD));
        if (StringUtils.isBlank(filter)) {
            return select.from(KOMPOSITION)
                         .orderBy(KOMPOSITION.TITEL.asc())
                         .offset(offset)
                         .limit(limit)
                         .fetch(KompositionService::toDto)
                         .stream();
        } else {
            return select.from(KOMPOSITION)
                         .where(filterCondition(filter))
                         .orderBy(KOMPOSITION.TITEL.asc())
                         .offset(offset)
                         .limit(limit)
                         .fetch(KompositionService::toDto)
                         .stream();
        }
    }

    private static KompositionDto toDto(org.jooq.Record it) {
        return new KompositionDto(it.get(KOMPOSITION.ID),
                                  it.get(KOMPOSITION.TITEL),
                                  it.get(KOMPOSITION.KOMPONIST),
                                  it.get(KOMPOSITION.ARRANGEUR),
                                  NotenFormat.valueOf(it.get(KOMPOSITION.FORMAT)),
                                  it.get(KOMPOSITION.AUDIO_SAMPLE),
                                  it.get(KOMPOSITION.COMMENT),
                                  it.get(NOTEN_COUNT_FIELD, Integer.class));
    }

    private int count(String filter) {
        if (StringUtils.isBlank(filter)) {
            return jooqDsl.fetchCount(KOMPOSITION);
        } else {
            return jooqDsl.fetchCount(KOMPOSITION, filterCondition(filter));
        }
    }

    private static Condition filterCondition(String filter) {
        return DSL.or(
                KOMPOSITION.TITEL.containsIgnoreCase(filter),
                KOMPOSITION.KOMPONIST.containsIgnoreCase(filter),
                KOMPOSITION.ARRANGEUR.containsIgnoreCase(filter)
        );
    }

}
