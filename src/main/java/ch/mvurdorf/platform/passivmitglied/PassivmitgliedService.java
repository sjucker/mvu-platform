package ch.mvurdorf.platform.passivmitglied;

import ch.mvurdorf.platform.jooq.tables.daos.PassivmitgliedDao;
import ch.mvurdorf.platform.jooq.tables.daos.PassivmitgliedPaymentDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Passivmitglied;
import ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedPayment;
import ch.mvurdorf.platform.utils.DateUtil;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

import static ch.mvurdorf.platform.jooq.Tables.PASSIVMITGLIED;
import static ch.mvurdorf.platform.jooq.Tables.PASSIVMITGLIED_PAYMENT;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.math.NumberUtils.toLong;
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
                                             PASSIVMITGLIED_PAYMENT.BEMERKUNG,
                                             PASSIVMITGLIED_PAYMENT.CREATED_AT,
                                             PASSIVMITGLIED_PAYMENT.CREATED_BY)
                                              .from(PASSIVMITGLIED_PAYMENT)
                                              .where(PASSIVMITGLIED_PAYMENT.FK_PASSIVMITGLIED.eq(PASSIVMITGLIED.ID))
                                              .orderBy(PASSIVMITGLIED_PAYMENT.DATUM.desc())
                              ).convertFrom(it -> it.map(mapping(PassivmitgliedPaymentDto::new)))
                      )
                      .from(PASSIVMITGLIED)
                      .where(filterCondition(filter))
                      .offset(offset)
                      .limit(limit)
                      .fetch(it -> {
                          var passivmitgliedRecord = it.value1();
                          return new PassivmitgliedDto(
                                  passivmitgliedRecord.getId(),
                                  passivmitgliedRecord.getExternalId(),
                                  passivmitgliedRecord.getVorname(),
                                  passivmitgliedRecord.getNachname(),
                                  passivmitgliedRecord.getStrasse(),
                                  passivmitgliedRecord.getOrt(),
                                  passivmitgliedRecord.getEmail(),
                                  passivmitgliedRecord.getBemerkung(),
                                  passivmitgliedRecord.getKommunikationPost(),
                                  passivmitgliedRecord.getKommunikationEmail(),
                                  passivmitgliedRecord.getRegisteredAt(),
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

        var filterAsLong = toLong(filter);
        return DSL.or(
                PASSIVMITGLIED.VORNAME.containsIgnoreCase(filter),
                PASSIVMITGLIED.NACHNAME.containsIgnoreCase(filter),
                PASSIVMITGLIED.EMAIL.containsIgnoreCase(filter),
                filterAsLong > 0 ? PASSIVMITGLIED.EXTERNAL_ID.contains(filterAsLong) : DSL.noCondition()
        );
    }

    public boolean exists(String email) {
        return passivmitgliedDao.fetchOptionalByEmail(email).isPresent();
    }

    public long create(PassivmitgliedDto passivmitglied) {
        var externalId = getNextExternalId();
        passivmitgliedDao.insert(new Passivmitglied(null,
                                                    passivmitglied.vorname(),
                                                    passivmitglied.nachname(),
                                                    passivmitglied.strasse(),
                                                    passivmitglied.ort(),
                                                    passivmitglied.email(),
                                                    passivmitglied.bemerkung(),
                                                    passivmitglied.kommunikationPost(),
                                                    passivmitglied.kommunikationEmail(),
                                                    DateUtil.now(),
                                                    externalId));
        return externalId;
    }

    private long getNextExternalId() {
        return jooqDsl.select(DSL.max(PASSIVMITGLIED.EXTERNAL_ID))
                      .from(PASSIVMITGLIED)
                      .fetchOptional()
                      .map(it -> it.value1() + 1L)
                      .orElse(1000001L);
    }

    public void addPayments(Long passivmitgliedId, List<PassivmitgliedPaymentDto> payments, String user) {
        payments.forEach(payment -> passivmitgliedPaymentDao.insert(new PassivmitgliedPayment(null,
                                                                                              passivmitgliedId,
                                                                                              payment.datum(),
                                                                                              payment.amount(),
                                                                                              payment.bemerkung(),
                                                                                              DateUtil.now(),
                                                                                              user)));
    }

}
