package ch.mvurdorf.platform.events;

import ch.mvurdorf.platform.common.AbsenzState;
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
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static ch.mvurdorf.platform.common.AbsenzState.UNKNOWN;
import static ch.mvurdorf.platform.jooq.Tables.ABSENZ_STATUS;
import static ch.mvurdorf.platform.jooq.tables.Event.EVENT;
import static ch.mvurdorf.platform.utils.DateUtil.now;
import static ch.mvurdorf.platform.utils.FormatUtil.DATE_FORMAT_SHORT;
import static ch.mvurdorf.platform.utils.FormatUtil.dayOfWeek;
import static ch.mvurdorf.platform.utils.FormatUtil.formatDate;
import static ch.mvurdorf.platform.utils.FormatUtil.formatTime;
import static java.time.format.TextStyle.FULL_STANDALONE;
import static java.time.format.TextStyle.SHORT_STANDALONE;
import static java.util.Locale.GERMAN;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
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
        return dataProvider(DSL.trueCondition());
    }

    public ConfigurableFilterDataProvider<EventDto, Void, String> dataProvider(Condition additionalCondition) {
        var dataProvider = DataProvider.<EventDto, String>fromFilteringCallbacks(
                query -> fetch(filterCondition(query.getFilter().orElse(null)).and(additionalCondition), query.getOffset(), query.getLimit()),
                query -> count(filterCondition(query.getFilter().orElse(null)).and(additionalCondition))
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
                            EventType.valueOf(it.getType()),
                            it.getInfoOnly(),
                            it.getRelevantForAbsenz(),
                            it.getRelevantForWebsite(),
                            it.getCreatedAt(),
                            it.getCreatedBy(),
                            it.getUpdatedAt(),
                            it.getUpdatedBy());
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
        var newEvent = toEvent(event);
        newEvent.setCreatedAt(now());
        newEvent.setCreatedBy(user);
        eventDao.insert(newEvent);
    }

    private static Event toEvent(EventDataDto event) {
        return new Event(event.getId(),
                         event.getFromDate(), event.getFromTime(),
                         event.getToDate(), event.getToTime(),
                         event.isApproximately(),
                         event.getTitle(),
                         event.getDescription(),
                         event.getLocation(),
                         event.getInterna(),
                         event.getLiterature(),
                         event.getType().name(),
                         event.isRelevantForAbsenz(),
                         event.isRelevantForWebsite(),
                         null, null, null, null, null, null,
                         event.isInfoOnly());
    }

    public void update(EventDataDto event, String user) {
        var currentVersion = eventDao.fetchOptionalById(event.getId()).orElseThrow();

        var eventToUpdate = toEvent(event);
        eventToUpdate.setCreatedAt(currentVersion.getCreatedAt());
        eventToUpdate.setCreatedBy(currentVersion.getCreatedBy());
        eventToUpdate.setUpdatedAt(now());
        eventToUpdate.setUpdatedBy(user);

        if (event.isTrackChanges()) {
            eventToUpdate.setId(null);
            eventDao.insert(eventToUpdate);

            currentVersion.setNextVersion(eventToUpdate.getId());
            eventDao.update(currentVersion);

            // make sure the existing AbsenzenStatus are transferred to the latest version
            jooqDsl.update(ABSENZ_STATUS)
                   .set(ABSENZ_STATUS.FK_EVENT, eventToUpdate.getId())
                   .where(ABSENZ_STATUS.FK_EVENT.eq(currentVersion.getId()))
                   .execute();
        } else {
            eventDao.update(eventToUpdate);
        }
    }

    public void delete(EventDto event, boolean permanent) {
        if (permanent) {
            log.info("deleting event {} permanently", event);
            jooqDsl.deleteFrom(ABSENZ_STATUS)
                   .where(ABSENZ_STATUS.FK_EVENT.eq(event.id()))
                   .execute();
            eventDao.deleteById(event.id());
        } else {
            log.info("mark event {} as deleted", event);
            var pojo = eventDao.findOptionalById(event.id()).orElseThrow();
            pojo.setDeletedAt(now());
            eventDao.update(pojo);
        }
    }

    public List<EventAbsenzStatusDto> findEventAbsenzenForUser(String email) {
        var login = loginDao.fetchOptionalByEmail(email).orElseThrow(() -> new NoSuchElementException("No login found for email " + email));
        return findEventAbsenzenForUser(login.getId());
    }

    public List<EventAbsenzStatusDto> findEventAbsenzenForUser(Long loginId) {
        return jooqDsl.select(EVENT.ID,
                              EVENT.FROM_DATE,
                              EVENT.FROM_TIME,
                              EVENT.TO_DATE,
                              EVENT.TO_TIME,
                              EVENT.APPROXIMATELY,
                              EVENT.TITLE,
                              EVENT.LOCATION,
                              EVENT.INTERNA,
                              EVENT.INFO_ONLY,
                              ABSENZ_STATUS.STATUS,
                              ABSENZ_STATUS.REMARK)
                      .from(EVENT)
                      .leftJoin(ABSENZ_STATUS).on(ABSENZ_STATUS.FK_EVENT.eq(EVENT.ID),
                                                  ABSENZ_STATUS.FK_LOGIN.eq(loginId))
                      .where(EVENT.NEXT_VERSION.isNull(),
                             EVENT.DELETED_AT.isNull(),
                             EVENT.FROM_DATE.ge(DateUtil.today()),
                             EVENT.RELEVANT_FOR_ABSENZ.isTrue()
                      )
                      .orderBy(EVENT.FROM_DATE.asc(), EVENT.FROM_TIME.asc(), EVENT.TO_DATE.asc(), EVENT.TO_TIME.asc())
                      .fetch(it -> new EventAbsenzStatusDto(loginId,
                                                            it.get(EVENT.ID),
                                                            title(it.get(EVENT.FROM_DATE), it.get(EVENT.FROM_TIME), it.get(EVENT.TO_DATE), it.get(EVENT.TO_TIME), it.get(EVENT.APPROXIMATELY)),
                                                            subtitle(it.get(EVENT.TITLE), it.get(EVENT.LOCATION)),
                                                            it.get(EVENT.INTERNA),
                                                            AbsenzState.of(it.get(ABSENZ_STATUS.STATUS)),
                                                            it.get(ABSENZ_STATUS.REMARK),
                                                            it.get(EVENT.TITLE),
                                                            it.get(EVENT.LOCATION),
                                                            from(it.get(EVENT.FROM_DATE), it.get(EVENT.FROM_TIME)),
                                                            to(it.get(EVENT.FROM_DATE), it.get(EVENT.TO_DATE), it.get(EVENT.TO_TIME)),
                                                            it.get(EVENT.INFO_ONLY)));
    }

    private LocalDateTime to(LocalDate fromDate, @Nullable LocalDate toDate, @Nullable LocalTime toTime) {
        if (toTime != null) {
            return LocalDateTime.of(toDate != null ? toDate : fromDate, toTime);
        }

        return LocalDateTime.of(toDate != null ? toDate : fromDate, LocalTime.MAX);
    }

    private LocalDateTime from(LocalDate fromDate, @Nullable LocalTime fromTime) {
        if (fromTime != null) {
            return LocalDateTime.of(fromDate, fromTime);
        }

        return LocalDateTime.of(fromDate, LocalTime.MIN);
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
        if (isNotBlank(location)) {
            subtitle += " (" + location + ")";
        }
        return subtitle;
    }

    public void updateEventAbsenzenForUser(String email, Long eventId, EventAbsenzStatusDto dto) {
        var login = loginDao.fetchOptionalByEmail(email).orElseThrow(() -> new NoSuchElementException("No login found for email " + email));
        updateEventAbsenzenForUser(login.getId(), eventId, dto.status(), dto.remark());
    }

    public void updateEventAbsenzenForUser(Long loginId, Long eventId, AbsenzState state, String remark) {
        var status = Optional.ofNullable(state).orElse(UNKNOWN).name();
        absenzStatusDao.findOptionalById(jooqDsl.newRecord(ABSENZ_STATUS.FK_LOGIN, ABSENZ_STATUS.FK_EVENT).values(loginId, eventId))
                       .ifPresentOrElse(pojo -> {
                                            pojo.setRemark(remark);
                                            pojo.setStatus(status);
                                            absenzStatusDao.update(pojo);
                                        },
                                        () -> absenzStatusDao.insert(new AbsenzStatus(loginId, eventId, remark, status)));

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
                                                      created || !Objects.equals(it.getFromDate(), firstVersion.getFromDate()) || !Objects.equals(it.getToDate(), firstVersion.getToDate()),
                                                      created || !Objects.equals(it.getFromTime(), firstVersion.getFromTime()) || !Objects.equals(it.getToTime(), firstVersion.getToTime()),
                                                      created || !Strings.CS.equals(stripToNull(it.getTitle()), stripToNull(firstVersion.getTitle())),
                                                      created || !Strings.CS.equals(stripToNull(it.getLocation()), stripToNull(firstVersion.getLocation())),
                                                      created || !Strings.CS.equals(stripToNull(it.getLiterature()), stripToNull(firstVersion.getLiterature())));
                     })
                     .toList();
    }

    private EventRecord findFirstVersion(EventRecord currentVersion, Map<Long, EventRecord> previousPerSuccessorId, LocalDate cutoffDate) {
        var previousVersion = previousPerSuccessorId.get(currentVersion.getId());
        if (previousVersion != null) {
            if (!getRelevantDate(previousVersion).isBefore(cutoffDate.atStartOfDay())) {
                return findFirstVersion(previousVersion, previousPerSuccessorId, cutoffDate);
            } else {
                return previousVersion;
            }
        }
        return currentVersion;
    }

    private static LocalDateTime getRelevantDate(EventRecord previousVersion) {
        return previousVersion.getUpdatedAt() != null ? previousVersion.getUpdatedAt() : previousVersion.getCreatedAt();
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
