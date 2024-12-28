package ch.mvurdorf.platform.passivmitglied;

import ch.mvurdorf.platform.jooq.tables.daos.PassivmitgliedDao;
import ch.mvurdorf.platform.jooq.tables.daos.PassivmitgliedPaymentDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Passivmitglied;
import ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedPayment;
import ch.mvurdorf.platform.service.QRBillService;
import ch.mvurdorf.platform.utils.DateUtil;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static ch.mvurdorf.platform.jooq.Tables.PASSIVMITGLIED;
import static ch.mvurdorf.platform.jooq.Tables.PASSIVMITGLIED_PAYMENT;
import static ch.mvurdorf.platform.jooq.Tables.PASSIVMITGLIED_VOUCHER;
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
    private final QRBillService qrBillService;

    public PassivmitgliedService(DSLContext jooqDsl,
                                 PassivmitgliedDao passivmitgliedDao,
                                 PassivmitgliedPaymentDao passivmitgliedPaymentDao,
                                 QRBillService qrBillService) {
        this.jooqDsl = jooqDsl;
        this.passivmitgliedDao = passivmitgliedDao;
        this.passivmitgliedPaymentDao = passivmitgliedPaymentDao;
        this.qrBillService = qrBillService;
    }

    public ConfigurableFilterDataProvider<PassivmitgliedDto, Void, String> dataProvider() {
        var dataProvider = DataProvider.<PassivmitgliedDto, String>fromFilteringCallbacks(
                query -> fetch(query.getFilter().orElse(null), query.getOffset(), query.getLimit()),
                query -> count(query.getFilter().orElse(null))
        );
        return dataProvider.withConfigurableFilter();
    }

    private Stream<PassivmitgliedDto> fetch(String filter, int offset, int limit) {
        return fetch(filterCondition(filter), offset, limit).stream();
    }

    private List<PassivmitgliedDto> fetch(Condition condition, int offset, int limit) {
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
                              ).convertFrom(it -> it.map(mapping(PassivmitgliedPaymentDto::new))),
                              multiset(
                                      select(PASSIVMITGLIED_VOUCHER.CODE,
                                             PASSIVMITGLIED_VOUCHER.DESCRIPTION,
                                             PASSIVMITGLIED_VOUCHER.VALID_UNTIL,
                                             PASSIVMITGLIED_VOUCHER.REDEEMED_AT,
                                             PASSIVMITGLIED_VOUCHER.REDEEMED_BY)
                                              .from(PASSIVMITGLIED_VOUCHER)
                                              .where(PASSIVMITGLIED_VOUCHER.FK_PASSIVMITGLIED.eq(PASSIVMITGLIED.ID))
                                              .orderBy(PASSIVMITGLIED_VOUCHER.VALID_UNTIL.desc())
                              ).convertFrom(it -> it.map(mapping(PassivmitgliedVoucherDto::new)))
                      )
                      .from(PASSIVMITGLIED)
                      .where(condition)
                      .offset(offset)
                      .limit(limit)
                      .fetch(it -> {
                          var passivmitgliedRecord = it.value1();
                          return new PassivmitgliedDto(
                                  passivmitgliedRecord.getId(),
                                  passivmitgliedRecord.getExternalId(),
                                  passivmitgliedRecord.getUuid(),
                                  passivmitgliedRecord.getAnrede(),
                                  passivmitgliedRecord.getVorname(),
                                  passivmitgliedRecord.getNachname(),
                                  passivmitgliedRecord.getStrasse(),
                                  passivmitgliedRecord.getOrt(),
                                  passivmitgliedRecord.getEmail(),
                                  passivmitgliedRecord.getBemerkung(),
                                  passivmitgliedRecord.getKommunikationPost(),
                                  passivmitgliedRecord.getKommunikationEmail(),
                                  passivmitgliedRecord.getRegisteredAt(),
                                  it.value2(),
                                  it.value3()
                          );
                      });
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
                                                    externalId,
                                                    passivmitglied.anrede(),
                                                    UUID.randomUUID().toString()));
        return externalId;
    }

    public void update(PassivmitgliedEditDTO passivmitglied) {
        var existing = passivmitgliedDao.findOptionalById(passivmitglied.getId()).orElseThrow();
        passivmitglied.applyTo(existing);
        passivmitgliedDao.update(existing);
    }

    private long getNextExternalId() {
        return jooqDsl.select(DSL.max(PASSIVMITGLIED.EXTERNAL_ID))
                      .from(PASSIVMITGLIED)
                      .fetchOptional(Record1::value1)
                      .map(it -> it + 1L)
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

    public Optional<byte[]> qrBill(Long externalId) {
        return passivmitgliedDao.fetchByExternalId(externalId).stream()
                                .findFirst()
                                .map(passivmitglied -> {
                                    var out = new ByteArrayOutputStream();
                                    qrBillService.passivmitglied(20.0, passivmitglied.getExternalId(), out);
                                    return out.toByteArray();
                                });
    }

    public Optional<PassivmitgliedDto> findByUUID(String uuid) {
        return fetch(PASSIVMITGLIED.UUID.equal(uuid), 0, 1).stream().findFirst();
    }
}
