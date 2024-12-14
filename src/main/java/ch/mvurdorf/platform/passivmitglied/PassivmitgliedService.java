package ch.mvurdorf.platform.passivmitglied;

import ch.mvurdorf.platform.jooq.tables.daos.PassivmitgliedDao;
import ch.mvurdorf.platform.jooq.tables.daos.PassivmitgliedPaymentDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Passivmitglied;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

import static ch.mvurdorf.platform.jooq.Tables.PASSIVMITGLIED;
import static ch.mvurdorf.platform.jooq.Tables.PASSIVMITGLIED_PAYMENT;
import static org.apache.commons.lang3.StringUtils.isBlank;
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

    public ConfigurableFilterDataProvider<PassivmitgliedDto, Void, String> dataProvider() {
        var dataProvider = DataProvider.<PassivmitgliedDto, String>fromFilteringCallbacks(
                query -> fetch(query.getFilter().orElse(null), query.getOffset(), query.getLimit()),
                query -> count(query.getFilter().orElse(null))
        );
        return dataProvider.withConfigurableFilter();
    }

    private Stream<PassivmitgliedDto> fetch(String filter, int offset, int limit) {
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
                      .where(filterCondition(filter))
                      .offset(offset)
                      .limit(limit)
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
                      })
                      .stream();
    }

    private int count(String filter) {
        return jooqDsl.fetchCount(PASSIVMITGLIED, filterCondition(filter));

    }

    private static Condition filterCondition(String filter) {
        if (isBlank(filter)) {
            return DSL.trueCondition();
        }
        return DSL.or(
                PASSIVMITGLIED.VORNAME.containsIgnoreCase(filter),
                PASSIVMITGLIED.NACHNAME.containsIgnoreCase(filter),
                PASSIVMITGLIED.EMAIL.containsIgnoreCase(filter)
        );
    }

    public boolean exists(String email) {
        return passivmitgliedDao.fetchOptionalByEmail(email).isPresent();
    }

    public void create(PassivmitgliedDto passivmitglied) {
        passivmitgliedDao.insert(new Passivmitglied(null,
                                                    passivmitglied.vorname(),
                                                    passivmitglied.nachname(),
                                                    passivmitglied.strasse(),
                                                    passivmitglied.ort(),
                                                    passivmitglied.email(),
                                                    passivmitglied.bemerkung(),
                                                    passivmitglied.kommunikationPost(),
                                                    passivmitglied.kommunikationEmail()));
    }

}
