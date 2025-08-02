package ch.mvurdorf.platform.events;

import ch.mvurdorf.platform.jooq.tables.daos.EventDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Event;
import ch.mvurdorf.platform.jooq.tables.records.EventRecord;
import ch.mvurdorf.platform.utils.DateUtil;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

import static ch.mvurdorf.platform.jooq.tables.Event.EVENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventsService {

    private final DSLContext jooqDsl;
    private final EventDao eventDao;

    public ConfigurableFilterDataProvider<EventDto, Void, String> dataProvider() {
        var dataProvider = DataProvider.<EventDto, String>fromFilteringCallbacks(
                query -> fetch(query.getFilter().orElse(null), query.getOffset(), query.getLimit()),
                query -> count(query.getFilter().orElse(null))
        );
        return dataProvider.withConfigurableFilter();
    }

    private Stream<EventDto> fetch(String filter, int offset, int limit) {
        return jooqDsl.selectFrom(EVENT)
                      .where(EVENT.NEXT_VERSION.isNull(),
                             EVENT.DELETED_AT.isNull(),
                             EVENT.FROM_DATE.ge(DateUtil.today()),
                             filterCondition(filter))
                      .orderBy(EVENT.FROM_DATE.asc(), EVENT.FROM_TIME.asc())
                      .offset(offset)
                      .limit(limit)
                      .fetch(EventsService::toDto)
                      .stream();
    }

    private static EventDto toDto(EventRecord it) {
        return new EventDto(it.getId(),
                            it.getFromDate(),
                            it.getFromTime(),
                            it.getToDate(),
                            it.getToTime(),
                            it.getApproximately(),
                            it.getTitle(),
                            it.getDescription(),
                            it.getLocation(),
                            it.getInterna(),
                            it.getLiterature(),
                            it.getType(),
                            it.getRelevantForAbsenz(),
                            it.getRelevantForWebsite(),
                            it.getCreatedAt(),
                            it.getCreatedBy());
    }

    private int count(String filter) {
        return jooqDsl.fetchCount(EVENT, EVENT.NEXT_VERSION.isNull(),
                                  EVENT.DELETED_AT.isNull(),
                                  EVENT.FROM_DATE.ge(DateUtil.today()),
                                  filterCondition(filter));
    }

    private static Condition filterCondition(String filter) {
        if (StringUtils.isBlank(filter)) {
            return DSL.trueCondition();
        }

        return DSL.or(
                EVENT.TITLE.containsIgnoreCase(filter),
                EVENT.LOCATION.containsIgnoreCase(filter),
                EVENT.DESCRIPTION.containsIgnoreCase(filter)
        );
    }

    public void insert(EventDataDto event, String user) {
        eventDao.insert(toEvent(event, user));
    }

    private static Event toEvent(EventDataDto event, String user) {
        return new Event(event.getId(),
                         event.getFromDate(), event.getFromTime(),
                         event.getToDate(), event.getToTime(),
                         event.isApproximately(),
                         event.getTitle(),
                         event.getDescription(),
                         event.getLocation(),
                         event.getInterna(),
                         event.getLiterature(),
                         event.getType(),
                         event.isRelevantForAbsenz(),
                         event.isRelevantForWebsite(),
                         DateUtil.now(), user,
                         null, null);
    }

    public void update(EventDataDto event, String user) {
        if (event.isTrackChanges()) {
            var currentVersion = eventDao.fetchOptionalById(event.getId()).orElseThrow();

            var nextVersion = toEvent(event, user);
            nextVersion.setId(null);
            eventDao.insert(nextVersion);

            currentVersion.setNextVersion(nextVersion.getId());
            eventDao.update(currentVersion);
        } else {
            eventDao.update(toEvent(event, user));
        }
    }

    // TODO delete
}
