package ch.mvurdorf.platform.passivmitglied;

import ch.mvurdorf.platform.jooq.tables.daos.PassivmitgliedDao;
import ch.mvurdorf.platform.jooq.tables.daos.PassivmitgliedPaymentDao;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.List;

import static ch.mvurdorf.platform.jooq.Tables.PASSIVMITGLIED;
import static ch.mvurdorf.platform.jooq.Tables.PASSIVMITGLIED_PAYMENT;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

@Service
public class PassivmitgliedService {
    private final DSLContext jooqDsl;
    private final PassivmitgliedDao passivmitgliedDao;
    private final PassivmitgliedPaymentDao passivmitgliedPaymentDao;

    public PassivmitgliedService(DSLContext jooqDsl,
                                 PassivmitgliedDao passivmitgliedDao,
                                 PassivmitgliedPaymentDao passivmitgliedPaymentDao) {
        this.jooqDsl = jooqDsl;
        this.passivmitgliedDao = passivmitgliedDao;
        this.passivmitgliedPaymentDao = passivmitgliedPaymentDao;
    }

    public List<PassivmitgliedDto> findAll() {
        return jooqDsl.select(
                              PASSIVMITGLIED,
                              multiset(
                                      select(PASSIVMITGLIED_PAYMENT.DATUM,
                                             PASSIVMITGLIED_PAYMENT.AMOUNT,
                                             PASSIVMITGLIED_PAYMENT.BEMERKUNG)
                                              .from(PASSIVMITGLIED_PAYMENT)
                                              .where(PASSIVMITGLIED_PAYMENT.FK_PASSIVMITGLIED.eq(PASSIVMITGLIED.ID))
                              ).convertFrom(it -> it.map(mapping(PassivmitgliedPaymentDto::new)))
                      )
                      .from(PASSIVMITGLIED)
                      .fetch(it -> {
                          var passivmitgliedRecord = it.value1();
                          return new PassivmitgliedDto(
                                  passivmitgliedRecord.getVorname(),
                                  passivmitgliedRecord.getNachname(),
                                  passivmitgliedRecord.getStrasse(),
                                  passivmitgliedRecord.getOrt(),
                                  passivmitgliedRecord.getEmail(),
                                  passivmitgliedRecord.getBemerkung(),
                                  passivmitgliedRecord.getKommunikationPost(),
                                  passivmitgliedRecord.getKommunikationEmail(),
                                  it.value2()
                          );
                      });
    }

}
