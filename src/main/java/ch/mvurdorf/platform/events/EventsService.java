package ch.mvurdorf.platform.events;

import ch.mvurdorf.platform.jooq.tables.daos.AbsenzStatusDao;
import ch.mvurdorf.platform.jooq.tables.daos.EventDao;
import ch.mvurdorf.platform.jooq.tables.daos.LoginDao;
import ch.mvurdorf.platform.jooq.tables.pojos.AbsenzStatus;
import ch.mvurdorf.platform.jooq.tables.pojos.Event;
import ch.mvurdorf.platform.jooq.tables.records.EventRecord;
import ch.mvurdorf.platform.utils.DateUtil;
import ch.mvurdorf.platform.utils.FormatUtil;
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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import static ch.mvurdorf.platform.events.AbsenzState.UNKNOWN;
import static ch.mvurdorf.platform.jooq.Tables.ABSENZ_STATUS;
import static ch.mvurdorf.platform.jooq.tables.Event.EVENT;
import static ch.mvurdorf.platform.utils.FormatUtil.DATE_FORMAT_SHORT;
import static ch.mvurdorf.platform.utils.FormatUtil.dayOfWeek;
import static ch.mvurdorf.platform.utils.FormatUtil.formatDate;
import static ch.mvurdorf.platform.utils.FormatUtil.formatTime;
import static java.time.format.TextStyle.FULL_STANDALONE;
import static java.time.format.TextStyle.SHORT_STANDALONE;
import static java.util.Locale.GERMAN;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.stripToNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventsService {

    private final DSLContext jooqDsl;
    private final EventDao eventDao;
    private final LoginDao loginDao;
    private final AbsenzStatusDao absenzStatusDao;

    public ConfigurableFilterDataProvider<EventDto, Void, String> dataProvider() {
        var dataProvider = DataProvider.<EventDto, String>fromFilteringCallbacks(
                query -> fetch(filterCondition(query.getFilter().orElse(null)), query.getOffset(), query.getLimit()),
                query -> count(filterCondition(query.getFilter().orElse(null)))
        );
        return dataProvider.withConfigurableFilter();
    }

    private Stream<EventDto> fetch(Condition condition, int offset, int limit) {
        return jooqDsl.selectFrom(EVENT)
                      .where(EVENT.NEXT_VERSION.isNull(),
                             EVENT.DELETED_AT.isNull(),
                             EVENT.FROM_DATE.ge(DateUtil.today()),
                             condition)
                      .orderBy(EVENT.FROM_DATE.asc(), EVENT.FROM_TIME.asc(), EVENT.TO_DATE.asc(), EVENT.TO_TIME.asc())
                      .offset(offset)
                      .limit(limit)
                      .fetch(EventsService::toDto)
                      .stream();
    }

    public List<EventDto> findAllFutureEventsForWebsite(Integer limit) {
        if (limit == null) {
            limit = Integer.MAX_VALUE;
        }
        return fetch(EVENT.RELEVANT_FOR_WEBSITE.isTrue(), 0, limit).toList();
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

    private int count(Condition condition) {
        return jooqDsl.fetchCount(EVENT, EVENT.NEXT_VERSION.isNull(),
                                  EVENT.DELETED_AT.isNull(),
                                  EVENT.FROM_DATE.ge(DateUtil.today()),
                                  condition);
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

            // TODO migrate all existing absenz_status to new version!
        } else {
            eventDao.update(toEvent(event, user));
        }
    }
    // TODO delete

    public List<EventAbsenzStatusDto> findEventAbsenzenForUser(String email) {
        var login = loginDao.fetchOptionalByEmail(email).orElseThrow(() -> new NoSuchElementException("No login found for email " + email));
        return jooqDsl.select(EVENT.ID,
                              EVENT.FROM_DATE,
                              EVENT.FROM_TIME,
                              EVENT.TO_DATE,
                              EVENT.TO_TIME,
                              EVENT.APPROXIMATELY,
                              EVENT.TITLE,
                              EVENT.LOCATION,
                              EVENT.INTERNA,
                              ABSENZ_STATUS.STATUS,
                              ABSENZ_STATUS.REMARK)
                      .from(EVENT)
                      .leftJoin(ABSENZ_STATUS).on(ABSENZ_STATUS.FK_EVENT.eq(EVENT.ID),
                                                  ABSENZ_STATUS.FK_LOGIN.eq(login.getId()))
                      .where(EVENT.NEXT_VERSION.isNull(),
                             EVENT.DELETED_AT.isNull(),
                             EVENT.FROM_DATE.ge(DateUtil.today()),
                             EVENT.RELEVANT_FOR_ABSENZ.isTrue()
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

    public void updateEventAbsenzenForUser(String email, Long eventId, EventAbsenzStatusDto dto) {
        var login = loginDao.fetchOptionalByEmail(email).orElseThrow(() -> new NoSuchElementException("No login found for email " + email));
        var status = Optional.ofNullable(dto.status()).orElse(UNKNOWN).name();
        absenzStatusDao.findOptionalById(jooqDsl.newRecord(ABSENZ_STATUS.FK_LOGIN, ABSENZ_STATUS.FK_EVENT).values(login.getId(), eventId))
                       .ifPresentOrElse(pojo -> {
                                            pojo.setRemark(dto.remark());
                                            pojo.setStatus(status);
                                            absenzStatusDao.update(pojo);
                                        },
                                        () -> absenzStatusDao.insert(new AbsenzStatus(login.getId(), eventId, dto.remark(), status)));

    }

    public List<ProbeplanEntryDto> getProbeplan(LocalDate cutoffDate) {
        var events = jooqDsl.selectFrom(EVENT)
                            .where(EVENT.FROM_DATE.ge(DateUtil.today()))
                            .orderBy(EVENT.FROM_DATE.asc(), EVENT.FROM_TIME.asc(), EVENT.TO_DATE.asc(), EVENT.TO_TIME.asc())
                            .fetch();

        var previousPerSuccessorId = events.stream()
                                           .filter(it -> it.getNextVersion() != null)
                                           .collect(toMap(EventRecord::getNextVersion, identity()));

        return events.stream()
                     .filter(it -> it.getNextVersion() == null)
                     .map(it -> {
                         // might be the same as the current version
                         var firstVersion = findFirstVersion(it, previousPerSuccessorId, cutoffDate);
                         var created = firstVersion.getCreatedAt().isAfter(cutoffDate.atStartOfDay());
                         return new ProbeplanEntryDto(it.getFromDate().getYear(),
                                                      it.getFromDate().getMonth().getDisplayName(FULL_STANDALONE, GERMAN),
                                                      it.getType(),
                                                      getDayOfMonth(it),
                                                      getDayOfWeek(it),
                                                      getTime(it),
                                                      it.getTitle(),
                                                      it.getLocation(),
                                                      it.getLiterature(),
                                                      it.getDeletedAt(),
                                                      created || it.getFromDate() != firstVersion.getFromDate() || it.getToDate() != firstVersion.getToDate(),
                                                      created || it.getFromTime() != firstVersion.getFromTime() || it.getToTime() != firstVersion.getToTime(),
                                                      created || !StringUtils.equals(it.getTitle(), firstVersion.getTitle()),
                                                      created || !StringUtils.equals(it.getLocation(), firstVersion.getLocation()),
                                                      created || !StringUtils.equals(stripToNull(it.getLiterature()), stripToNull(firstVersion.getLiterature())));
                     })
                     .toList();
    }

    private EventRecord findFirstVersion(EventRecord currentVersion, Map<Long, EventRecord> previousPerSuccessorId, LocalDate cutoffDate) {
        var previousVersion = previousPerSuccessorId.get(currentVersion.getId());
        if (previousVersion != null && !previousVersion.getFromDate().isBefore(cutoffDate)) {
            return findFirstVersion(previousVersion, previousPerSuccessorId, cutoffDate);
        }
        return currentVersion;
    }

    private static String getTime(EventRecord it) {
        if (it.getFromTime() != null && it.getToTime() != null) {
            return "%s-%s".formatted(FormatUtil.formatTime(it.getFromTime()), FormatUtil.formatTime(it.getToTime()));
        }
        if (it.getFromTime() != null) {
            return FormatUtil.formatTime(it.getFromTime());
        }
        return "";
    }

    private static String getDayOfWeek(EventRecord it) {
        var from = it.getFromDate().getDayOfWeek().getDisplayName(SHORT_STANDALONE, GERMAN);
        if (it.getToDate() != null && it.getToDate().isAfter(it.getFromDate())) {
            return "%s-%s".formatted(from, it.getToDate().getDayOfWeek().getDisplayName(SHORT_STANDALONE, GERMAN));
        }
        return from;
    }

    private static String getDayOfMonth(EventRecord it) {
        var from = it.getFromDate().getDayOfMonth();
        if (it.getToDate() != null && it.getToDate().isAfter(it.getFromDate())) {
            return "%02d.-%02d.".formatted(from, it.getToDate().getDayOfMonth());
        }

        return "%02d.".formatted(from);
    }
}
