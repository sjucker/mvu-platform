package ch.mvurdorf.platform.events;

import ch.mvurdorf.platform.jooq.tables.daos.EventDao;
import ch.mvurdorf.platform.jooq.tables.daos.LoginDao;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static ch.mvurdorf.platform.jooq.Tables.ABSENZ_STATUS;
import static ch.mvurdorf.platform.jooq.tables.Event.EVENT;
import static ch.mvurdorf.platform.utils.FormatUtil.DATE_FORMAT_SHORT;
import static ch.mvurdorf.platform.utils.FormatUtil.dayOfWeek;
import static ch.mvurdorf.platform.utils.FormatUtil.formatDate;
import static ch.mvurdorf.platform.utils.FormatUtil.formatTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventsService {

    private final DSLContext jooqDsl;
    private final EventDao eventDao;
    private final LoginDao loginDao;

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
                      .orderBy(EVENT.FROM_DATE.asc(), EVENT.FROM_TIME.asc(), EVENT.TO_DATE.asc(), EVENT.TO_TIME.asc())
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

    public List<EventAbsenzStatusDto> findEventAbsenzenForUser(String email) {
        var login = loginDao.fetchOptionalByEmail(email).orElseThrow(() -> new NoSuchElementException("No login found for email " + email));
        return jooqDsl.select(EVENT,
                              ABSENZ_STATUS.STATUS,
                              ABSENZ_STATUS.REMARK)
                      .from(EVENT)
                      .leftJoin(ABSENZ_STATUS).on(ABSENZ_STATUS.FK_EVENT.eq(EVENT.ID))
                      .where(ABSENZ_STATUS.FK_LOGIN.eq(login.getId()),
                             EVENT.NEXT_VERSION.isNull(),
                             EVENT.DELETED_AT.isNull(),
                             EVENT.FROM_DATE.ge(DateUtil.today())
                      )
                      .orderBy(EVENT.FROM_DATE.asc(), EVENT.FROM_TIME.asc(), EVENT.TO_DATE.asc(), EVENT.TO_TIME.asc())
                      .fetch(it -> new EventAbsenzStatusDto(login.getId(),
                                                            it.get(EVENT.ID),
                                                            title(it.get(EVENT.FROM_DATE), it.get(EVENT.FROM_TIME), it.get(EVENT.TO_DATE), it.get(EVENT.TO_TIME), it.get(EVENT.APPROXIMATELY)),
                                                            subtitle(it.get(EVENT.TITLE), it.get(EVENT.LOCATION)),
                                                            it.get(EVENT.INTERNA),
                                                            AbsenzState.of(it.get(ABSENZ_STATUS.STATUS)),
                                                            it.get(ABSENZ_STATUS.REMARK)));
    }

    private String title(LocalDate fromDate, LocalTime fromTime, LocalDate toDate, LocalTime toTime, boolean approximately) {

        var dayOfWeek = dayOfWeek(fromDate);
        var date = formatDate(fromDate, DATE_FORMAT_SHORT);

        if (toDate != null && toDate.isAfter(fromDate)) {
            dayOfWeek += "-" + dayOfWeek(toDate);
            date += "-" + formatDate(toDate, DATE_FORMAT_SHORT);
        }

        var title = dayOfWeek + ", " + date;

        if (fromTime != null) {
            var time = formatTime(fromTime);
            if (approximately) {
                time = "ca. " + time;
            }

            if (toTime != null) {
                time += "-" + formatTime(toTime);
            }

            title += ", " + time;
        }

        return title;
    }

    private String subtitle(String title, String location) {
        var subtitle = title;
        if (StringUtils.isNotBlank(location)) {
            subtitle += " (" + location + ")";
        }
        return subtitle;
    }
}
