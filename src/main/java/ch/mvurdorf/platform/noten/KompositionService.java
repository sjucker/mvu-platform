package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.jooq.tables.daos.KompositionDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Komposition;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

import static ch.mvurdorf.platform.jooq.tables.Komposition.KOMPOSITION;

@Service
public class KompositionService {
    private final DSLContext jooqDsl;
    private final KompositionDao kompositionDao;

    public KompositionService(DSLContext jooqDsl, KompositionDao kompositionDao) {
        this.jooqDsl = jooqDsl;
        this.kompositionDao = kompositionDao;
    }

    public void insert(KompositionDto komposition) {
        kompositionDao.insert(new Komposition(null, komposition.titel(), komposition.komponist(), komposition.arrangeur(), komposition.durationInSeconds()));
    }

    public ConfigurableFilterDataProvider<KompositionDto, Void, String> dataProvider() {
        var dataProvider = DataProvider.<KompositionDto, String>fromFilteringCallbacks(
                query -> fetch(query.getFilter().orElse(null), query.getOffset(), query.getLimit()),
                query -> count(query.getFilter().orElse(null))
        );
        return dataProvider.withConfigurableFilter();
    }

    private Stream<KompositionDto> fetch(String filter, int offset, int limit) {
        if (StringUtils.isBlank(filter)) {
            return jooqDsl.select()
                          .from(KOMPOSITION)
                          .orderBy(KOMPOSITION.TITEL.asc())
                          .offset(offset)
                          .limit(limit)
                          .fetch(KompositionService::toDto)
                          .stream();
        } else {
            return jooqDsl.select()
                          .from(KOMPOSITION)
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
                                  it.get(KOMPOSITION.DURATION_IN_SECONDS));
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
