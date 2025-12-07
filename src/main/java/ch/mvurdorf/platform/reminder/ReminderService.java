package ch.mvurdorf.platform.reminder;

import ch.mvurdorf.platform.service.MailService;
import ch.mvurdorf.platform.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static ch.mvurdorf.platform.common.AbsenzState.UNKNOWN;
import static ch.mvurdorf.platform.jooq.Tables.ABSENZ_STATUS;
import static ch.mvurdorf.platform.jooq.Tables.EVENT;
import static ch.mvurdorf.platform.jooq.Tables.LOGIN;
import static ch.mvurdorf.platform.utils.DateUtil.ZURICH;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final DSLContext jooqDsl;
    private final MailService mailService;

    @Scheduled(cron = "0 0 8 * * Sun", zone = ZURICH)
    public void sendReminders() {
        jooqDsl.selectDistinct(LOGIN.EMAIL)
               .from(EVENT)
               .crossJoin(LOGIN)
               .leftJoin(ABSENZ_STATUS).on(ABSENZ_STATUS.FK_LOGIN.eq(LOGIN.ID),
                                           ABSENZ_STATUS.FK_EVENT.eq(EVENT.ID))
               .where(EVENT.NEXT_VERSION.isNull(),
                      EVENT.DELETED_AT.isNull(),
                      EVENT.FROM_DATE.ge(DateUtil.today()),
                      EVENT.FROM_DATE.le(DateUtil.today().plusMonths(1)),
                      EVENT.RELEVANT_FOR_ABSENZ.isTrue(),
                      LOGIN.ACTIVE.isTrue(),
                      LOGIN.SEND_REMINDER.isTrue(),
                      DSL.or(ABSENZ_STATUS.STATUS.isNull(),
                             ABSENZ_STATUS.STATUS.eq(UNKNOWN.name())))
               .forEach(it -> mailService.sendReminderEmail(it.get(LOGIN.EMAIL)));
    }
}
