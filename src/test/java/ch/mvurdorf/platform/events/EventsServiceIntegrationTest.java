package ch.mvurdorf.platform.events;

import ch.mvurdorf.platform.common.AbsenzState;
import ch.mvurdorf.platform.jooq.tables.daos.AbsenzStatusDao;
import ch.mvurdorf.platform.jooq.tables.daos.EventDao;
import ch.mvurdorf.platform.jooq.tables.daos.LoginDao;
import ch.mvurdorf.platform.testing.AbstractIntegrationTest;
import com.vaadin.flow.data.provider.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.UUID;

import static ch.mvurdorf.platform.jooq.Tables.ABSENZ_STATUS;
import static ch.mvurdorf.platform.jooq.Tables.EVENT;
import static ch.mvurdorf.platform.jooq.Tables.LOGIN;
import static ch.mvurdorf.platform.utils.DateUtil.today;
import static org.assertj.core.api.Assertions.assertThat;

class EventsServiceIntegrationTest extends AbstractIntegrationTest {

    private EventsService eventsService;
    private Long loginId;

    @BeforeEach
    void setUp() {
        var configuration = jooqDsl.configuration();
        eventsService = new EventsService(jooqDsl, new EventDao(configuration), new LoginDao(configuration), new AbsenzStatusDao(configuration));

        loginId = jooqDsl.insertInto(LOGIN)
                         .set(LOGIN.NAME, "Test")
                         .set(LOGIN.EMAIL, "test@mvurdorf.ch")
                         .set(LOGIN.ACTIVE, true)
                         .set(LOGIN.CALENDAR_TOKEN, UUID.randomUUID().toString())
                         .returning(LOGIN.ID)
                         .fetchOne(LOGIN.ID);
    }

    @Test
    void deletePermanentlyWithoutPreviousVersions() {
        eventsService.insert(event("Probe"), "test");
        var event = currentEvent();
        eventsService.updateEventAbsenzenForUser(loginId, event.id(), AbsenzState.POSITIVE, null);

        eventsService.delete(event, true);

        assertThat(jooqDsl.fetchCount(EVENT)).isZero();
        assertThat(jooqDsl.fetchCount(ABSENZ_STATUS)).isZero();
    }

    @Test
    void deletePermanentlyWithMultiplePreviousVersions() {
        eventsService.insert(event("Probe"), "test");
        updateWithTrackChanges("Probe 2");
        updateWithTrackChanges("Probe 3");
        var event = currentEvent();
        eventsService.updateEventAbsenzenForUser(loginId, event.id(), AbsenzState.POSITIVE, null);
        assertThat(jooqDsl.fetchCount(EVENT)).isEqualTo(3);

        eventsService.delete(event, true);

        assertThat(jooqDsl.fetchCount(EVENT)).isZero();
        assertThat(jooqDsl.fetchCount(ABSENZ_STATUS)).isZero();
    }

    @Test
    void deletePermanentlyKeepsOtherEvents() {
        eventsService.insert(event("Andere Probe"), "test");
        var otherEvent = currentEvent();
        updateWithTrackChanges("Andere Probe 2");

        eventsService.insert(event("Probe"), "test");
        var event = eventsService.dataProvider().fetch(new Query<>()).filter(it -> it.title().equals("Probe")).findFirst().orElseThrow();
        eventsService.delete(event, true);

        assertThat(jooqDsl.fetchCount(EVENT)).isEqualTo(2);
        assertThat(jooqDsl.fetchCount(EVENT, EVENT.ID.eq(otherEvent.id()))).isOne();
    }

    @Test
    void deleteTraceableMarksOnlyCurrentVersionAsDeleted() {
        eventsService.insert(event("Probe"), "test");
        updateWithTrackChanges("Probe 2");
        var event = currentEvent();

        eventsService.delete(event, false);

        assertThat(jooqDsl.fetchCount(EVENT)).isEqualTo(2);
        assertThat(jooqDsl.fetchCount(EVENT, EVENT.DELETED_AT.isNotNull())).isOne();
        assertThat(jooqDsl.fetchCount(EVENT, EVENT.DELETED_AT.isNotNull(), EVENT.ID.eq(event.id()))).isOne();
    }

    private void updateWithTrackChanges(String title) {
        var data = EventDataDto.of(currentEvent());
        data.setTitle(title);
        eventsService.update(data, "test");
    }

    private EventDto currentEvent() {
        var events = eventsService.dataProvider().fetch(new Query<>()).toList();
        assertThat(events).hasSize(1);
        return events.getFirst();
    }

    private static EventDataDto event(String title) {
        var event = EventDataDto.newEvent();
        event.setFromDate(today().plusDays(7));
        event.setFromTime(LocalTime.of(20, 0));
        event.setTitle(title);
        event.setType(EventType.BLUE);
        return event;
    }
}
