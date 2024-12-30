package ch.mvurdorf.platform.konzerte;

import ch.mvurdorf.platform.jooq.tables.daos.KompositionDao;
import ch.mvurdorf.platform.jooq.tables.daos.KonzertDao;
import ch.mvurdorf.platform.jooq.tables.daos.KonzertEntryDao;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static ch.mvurdorf.platform.jooq.Tables.KOMPOSITION;
import static ch.mvurdorf.platform.jooq.Tables.KONZERT;
import static ch.mvurdorf.platform.jooq.Tables.KONZERT_ENTRY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

@Slf4j
@Service
@RequiredArgsConstructor
public class KonzerteService {

    private final DSLContext jooqDsl;
    private final KonzertDao konzertDao;
    private final KonzertEntryDao konzertEntryDao;
    private final KompositionDao kompositionDao;

    public ConfigurableFilterDataProvider<KonzertDto, Void, String> dataProvider() {
        var dataProvider = DataProvider.<KonzertDto, String>fromFilteringCallbacks(
                query -> fetch(query.getFilter().orElse(null), query.getOffset(), query.getLimit()),
                query -> count(query.getFilter().orElse(null))
        );
        return dataProvider.withConfigurableFilter();
    }

    private Stream<KonzertDto> fetch(String filter, int offset, int limit) {
        return fetch(filterCondition(filter), offset, limit).stream();
    }

    private int count(String filter) {
        return jooqDsl.fetchCount(KONZERT, filterCondition(filter));

    }

    private static Condition filterCondition(String filter) {
        if (isBlank(filter)) {
            return DSL.trueCondition();
        }

        return DSL.or(
                KONZERT.NAME.containsIgnoreCase(filter),
                KONZERT.DESCRIPTION.containsIgnoreCase(filter),
                KONZERT.LOCATION.containsIgnoreCase(filter)
        );
    }

    private List<KonzertDto> fetch(Condition condition, int offset, int limit) {
        return jooqDsl.select(
                              KONZERT,
                              multiset(
                                      select(KONZERT_ENTRY.DESCRIPTION,
                                             KONZERT_ENTRY.INDEX,
                                             KOMPOSITION.ID,
                                             KOMPOSITION.TITEL,
                                             KOMPOSITION.ARRANGEUR)
                                              .from(KONZERT_ENTRY)
                                              .leftJoin(KOMPOSITION).on(KONZERT_ENTRY.FK_KOMPOSITION.eq(KOMPOSITION.ID))
                                              .where(KONZERT_ENTRY.FK_KONZERT.eq(KONZERT.ID))
                                              .orderBy(KONZERT_ENTRY.INDEX.asc())
                              ).convertFrom(it -> it.map(mapping(KonzertEntryDto::new)))
                      )
                      .from(KONZERT)
                      .where(condition)
                      .orderBy(KONZERT.DATUM.desc())
                      .offset(offset)
                      .limit(limit)
                      .fetch(it -> {
                          var konzertRecord = it.value1();
                          return new KonzertDto(
                                  konzertRecord.getName(),
                                  konzertRecord.getDatum(),
                                  konzertRecord.getZeit(),
                                  konzertRecord.getLocation(),
                                  konzertRecord.getLocation(),
                                  it.value2()
                          );
                      });
    }

    public List<KonzertEntryDto> getAvailableKompositions() {
        return kompositionDao.findAll().stream()
                             .map(it -> new KonzertEntryDto(null, 0, it.getId(), it.getTitel(), it.getArrangeur()))
                             .sorted(Comparator.comparing(KonzertEntryDto::kompositionTitel).thenComparing(KonzertEntryDto::kompositionArrangeur))
                             .toList();
    }
}