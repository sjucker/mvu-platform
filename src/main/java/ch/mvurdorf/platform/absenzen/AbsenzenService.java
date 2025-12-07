package ch.mvurdorf.platform.absenzen;

import ch.mvurdorf.platform.common.AbsenzState;
import ch.mvurdorf.platform.common.Register;
import ch.mvurdorf.platform.jooq.tables.daos.AbsenzStatusDao;
import ch.mvurdorf.platform.jooq.tables.pojos.AbsenzStatus;
import ch.mvurdorf.platform.utils.DateUtil;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static ch.mvurdorf.platform.common.AbsenzState.NEGATIVE;
import static ch.mvurdorf.platform.common.AbsenzState.POSITIVE;
import static ch.mvurdorf.platform.jooq.Tables.ABSENZ_STATUS;
import static ch.mvurdorf.platform.jooq.Tables.EVENT;
import static ch.mvurdorf.platform.jooq.Tables.LOGIN;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbsenzenService {

    private final DSLContext jooqDsl;
    private final AbsenzStatusDao absenzStatusDao;

    public ConfigurableFilterDataProvider<EventAbsenzSummaryDto, Void, String> dataProvider() {
        var dataProvider = DataProvider.<EventAbsenzSummaryDto, String>fromFilteringCallbacks(
                query -> fetch(query.getOffset(), query.getLimit()),
                _ -> count()
        );
        return dataProvider.withConfigurableFilter();
    }

    private Stream<EventAbsenzSummaryDto> fetch(int offset, int limit) {
        var positiveCount = DSL.field(DSL.selectCount().from(ABSENZ_STATUS).where(ABSENZ_STATUS.FK_EVENT.eq(EVENT.ID), ABSENZ_STATUS.STATUS.eq(POSITIVE.name())));
        var negativeCount = DSL.field(DSL.selectCount().from(ABSENZ_STATUS).where(ABSENZ_STATUS.FK_EVENT.eq(EVENT.ID), ABSENZ_STATUS.STATUS.eq(NEGATIVE.name())));

        return jooqDsl.select(EVENT.ID,
                              EVENT.FROM_DATE,
                              EVENT.TITLE,
                              positiveCount,
                              negativeCount)
                      .from(EVENT)
                      .where(EVENT.NEXT_VERSION.isNull(),
                             EVENT.DELETED_AT.isNull(),
                             EVENT.FROM_DATE.ge(DateUtil.today()),
                             EVENT.RELEVANT_FOR_ABSENZ.isTrue(),
                             EVENT.INFO_ONLY.isFalse())
                      .orderBy(EVENT.FROM_DATE.asc(), EVENT.FROM_TIME.asc(), EVENT.TO_DATE.asc(), EVENT.TO_TIME.asc())
                      .offset(offset)
                      .limit(limit)
                      .fetch(it -> new EventAbsenzSummaryDto(it.get(EVENT.ID),
                                                             it.get(EVENT.FROM_DATE),
                                                             it.get(EVENT.TITLE),
                                                             it.get(positiveCount),
                                                             it.get(negativeCount)))
                      .stream();
    }

    private int count() {
        return jooqDsl.fetchCount(EVENT, EVENT.NEXT_VERSION.isNull(),
                                  EVENT.DELETED_AT.isNull(),
                                  EVENT.FROM_DATE.ge(DateUtil.today()),
                                  EVENT.RELEVANT_FOR_ABSENZ.isTrue(),
                                  EVENT.INFO_ONLY.isFalse());
    }

    public Map<Register, List<AbsenzStatusDto>> findAbsenzenStatusByEvent(Long eventId) {
        return jooqDsl.select(EVENT.ID,
                              LOGIN.ID,
                              LOGIN.REGISTER,
                              LOGIN.NAME,
                              ABSENZ_STATUS.REMARK,
                              ABSENZ_STATUS.STATUS)
                      .from(EVENT)
                      .crossJoin(LOGIN)
                      .leftJoin(ABSENZ_STATUS).on(ABSENZ_STATUS.FK_LOGIN.eq(LOGIN.ID),
                                                  ABSENZ_STATUS.FK_EVENT.eq(EVENT.ID))
                      .where(EVENT.ID.eq(eventId),
                             LOGIN.ACTIVE.isTrue())
                      .fetch(it -> new AbsenzStatusDto(it.get(EVENT.ID),
                                                       it.get(LOGIN.ID),
                                                       Register.valueOf(it.get(LOGIN.REGISTER)),
                                                       it.get(LOGIN.NAME),
                                                       it.get(ABSENZ_STATUS.REMARK),
                                                       AbsenzState.of(it.get(ABSENZ_STATUS.STATUS))))
                      .stream()
                      .collect(groupingBy(AbsenzStatusDto::register,
                                          collectingAndThen(toList(), list -> list.stream().sorted(comparing(AbsenzStatusDto::name)).toList())));
    }

    public void upsert(Long eventId, Long loginId, AbsenzState state) {
        absenzStatusDao.findOptionalById(jooqDsl.newRecord(ABSENZ_STATUS.FK_LOGIN, ABSENZ_STATUS.FK_EVENT).values(loginId, eventId))
                       .ifPresentOrElse(absenzStatus -> {
                                            absenzStatus.setStatus(state.name());
                                            absenzStatusDao.update(absenzStatus);
                                        },
                                        () -> {
                                            var absenzStatus = new AbsenzStatus(loginId, eventId, null, state.name());
                                            absenzStatusDao.insert(absenzStatus);
                                        });
    }
}
