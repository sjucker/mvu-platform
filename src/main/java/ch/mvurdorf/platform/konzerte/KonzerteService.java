package ch.mvurdorf.platform.konzerte;

import ch.mvurdorf.platform.jooq.tables.daos.KonzertDao;
import ch.mvurdorf.platform.jooq.tables.daos.KonzertEntryDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Konzert;
import ch.mvurdorf.platform.jooq.tables.pojos.KonzertEntry;
import ch.mvurdorf.platform.utils.DateUtil;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static ch.mvurdorf.platform.jooq.Tables.KOMPOSITION;
import static ch.mvurdorf.platform.jooq.Tables.KONZERT;
import static ch.mvurdorf.platform.jooq.Tables.KONZERT_ENTRY;
import static ch.mvurdorf.platform.jooq.Tables.REPERTOIRE;
import static ch.mvurdorf.platform.jooq.Tables.REPERTOIRE_ENTRY;
import static ch.mvurdorf.platform.repertoire.RepertoireType.MARSCHBUCH;
import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

@Slf4j
@Service
@RequiredArgsConstructor
public class KonzerteService {

    private final DSLContext jooqDsl;
    private final KonzertDao konzertDao;
    private final KonzertEntryDao konzertEntryDao;

    public ConfigurableFilterDataProvider<KonzertDto, Void, KonzerteFilter> dataProvider() {
        var dataProvider = DataProvider.<KonzertDto, KonzerteFilter>fromFilteringCallbacks(
                query -> fetch(query.getFilter().orElse(KonzerteFilter.empty()), query.getOffset(), query.getLimit()),
                query -> count(query.getFilter().orElse(KonzerteFilter.empty()))
        );
        return dataProvider.withConfigurableFilter();
    }

    private Stream<KonzertDto> fetch(KonzerteFilter filter, int offset, int limit) {
        return fetch(filterCondition(filter), offset, limit).stream();
    }

    private int count(KonzerteFilter filter) {
        return jooqDsl.fetchCount(KONZERT, filterCondition(filter));

    }

    private static Condition filterCondition(KonzerteFilter filter) {
        var condition = filter.isIncludingPast() ? DSL.trueCondition() : KONZERT.DATUM.greaterOrEqual(DateUtil.today());

        var textFilter = filter.getTextFilter();
        if (isNotBlank(textFilter)) {
            condition = condition.and(DSL.or(
                    KONZERT.NAME.containsIgnoreCase(textFilter),
                    KONZERT.DESCRIPTION.containsIgnoreCase(textFilter),
                    KONZERT.LOCATION.containsIgnoreCase(textFilter)
            ));
        }

        return condition;
    }

    private List<KonzertDto> fetch(Condition condition, int offset, int limit) {
        Field<BigDecimal> marschbuchNumber = select(max(REPERTOIRE_ENTRY.NUMBER)).from(REPERTOIRE_ENTRY)
                                                                                 .join(REPERTOIRE).on(REPERTOIRE_ENTRY.FK_REPERTOIRE.eq(REPERTOIRE.ID),
                                                                                                      REPERTOIRE.ID.eq(select(max(REPERTOIRE.ID)).from(REPERTOIRE).where(REPERTOIRE.TYPE.eq(MARSCHBUCH.name()))))
                                                                                 .where(REPERTOIRE_ENTRY.FK_KOMPOSITION.eq(KOMPOSITION.ID))
                                                                                 .asField();
        return jooqDsl.select(
                              KONZERT,
                              multiset(
                                      select(KONZERT_ENTRY.INDEX,
                                             marschbuchNumber,
                                             KONZERT_ENTRY.PLACEHOLDER,
                                             KOMPOSITION.ID,
                                             KOMPOSITION.TITEL,
                                             KOMPOSITION.KOMPONIST,
                                             KOMPOSITION.ARRANGEUR,
                                             KOMPOSITION.AUDIO_SAMPLE,
                                             KONZERT_ENTRY.ZUGABE)
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
                                  konzertRecord.getId(),
                                  konzertRecord.getName(),
                                  konzertRecord.getDatum(),
                                  konzertRecord.getZeit(),
                                  konzertRecord.getLocation(),
                                  konzertRecord.getDescription(),
                                  konzertRecord.getTenu(),
                                  it.value2()
                          );
                      });
    }

    @Transactional
    public void upsert(KonzertDto dto) {
        Long konzertId;
        if (dto.id() != null) {
            var konzert = konzertDao.findOptionalById(dto.id()).orElseThrow();
            konzert.setName(dto.name());
            konzert.setDatum(dto.datum());
            konzert.setZeit(dto.zeit());
            konzert.setLocation(dto.location());
            konzert.setDescription(dto.description());
            konzert.setTenu(dto.tenu());
            konzertDao.update(konzert);
            konzertId = konzert.getId();
            // delete all existing entries and re-insert new
            jooqDsl.deleteFrom(KONZERT_ENTRY)
                   .where(KONZERT_ENTRY.FK_KONZERT.eq(konzertId))
                   .execute();
        } else {
            var konzert = new Konzert(null, dto.name(), dto.datum(), dto.zeit(), dto.location(), dto.description(), dto.tenu());
            konzertDao.insert(konzert);
            konzertId = konzert.getId();
        }

        var index = 1;
        for (var entry : dto.entries()) {
            konzertEntryDao.insert(new KonzertEntry(null, konzertId, index++, entry.kompositionId(), entry.placeholder(), entry.zugabe()));
        }
    }

    @Transactional
    public void delete(KonzertDto dto) {
        log.info("deleting konzert {}", dto);
        konzertDao.deleteById(dto.id());
    }

    public List<KonzertDto> getFutureKonzerte() {
        return fetch(KONZERT.DATUM.greaterOrEqual(DateUtil.today()), 0, 10).stream()
                                                                           .sorted(comparing(KonzertDto::datumZeit))
                                                                           .toList();
    }

    public Optional<KonzertDto> findById(Long id) {
        return fetch(KONZERT.ID.eq(id), 0, 1).stream().findFirst();
    }

    public static String getNumber(KonzertEntryDto dto, List<KonzertEntryDto> entries) {
        if (dto.isPlaceholder()) {
            return null;
        }

        int number = 1;
        for (var entry : entries) {
            if (Objects.equals(entry.index(), dto.index())) {
                break;
            }
            if (!entry.isPlaceholder()) {
                if (dto.zugabe()) {
                    if (entry.zugabe()) {
                        number++;
                    }
                } else {
                    number++;
                }
            }
        }

        return (dto.zugabe() ? "Z%d" : "%d").formatted(number);
    }

    @Data
    @AllArgsConstructor
    public static final class KonzerteFilter {
        private String textFilter;
        private boolean includingPast;

        public static KonzerteFilter empty() {
            return new KonzerteFilter(null, false);
        }
    }
}
