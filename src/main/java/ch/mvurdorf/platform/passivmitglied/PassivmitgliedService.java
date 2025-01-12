package ch.mvurdorf.platform.passivmitglied;

import ch.mvurdorf.platform.jooq.tables.daos.PassivmitgliedDao;
import ch.mvurdorf.platform.jooq.tables.daos.PassivmitgliedPaymentDao;
import ch.mvurdorf.platform.jooq.tables.daos.PassivmitgliedVoucherDao;
import ch.mvurdorf.platform.jooq.tables.daos.VoucherDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Passivmitglied;
import ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedPayment;
import ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher;
import ch.mvurdorf.platform.jooq.tables.pojos.Voucher;
import ch.mvurdorf.platform.service.MailService;
import ch.mvurdorf.platform.service.QRBillService;
import ch.mvurdorf.platform.service.QRCodeService;
import ch.mvurdorf.platform.utils.DateUtil;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static ch.mvurdorf.platform.jooq.Tables.PASSIVMITGLIED;
import static ch.mvurdorf.platform.jooq.Tables.PASSIVMITGLIED_PAYMENT;
import static ch.mvurdorf.platform.jooq.Tables.PASSIVMITGLIED_VOUCHER;
import static java.util.Locale.ROOT;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.math.NumberUtils.toLong;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

@Service
@RequiredArgsConstructor
public class PassivmitgliedService {
    private static final double PASSIVMITGLIED_AMOUNT = 20.0;

    private final DSLContext jooqDsl;
    private final PassivmitgliedDao passivmitgliedDao;
    private final VoucherDao voucherDao;
    private final PassivmitgliedPaymentDao passivmitgliedPaymentDao;
    private final PassivmitgliedVoucherDao passivmitgliedVoucherDao;
    private final QRBillService qrBillService;
    private final QRCodeService qrCodeService;
    private final MailService mailService;

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

    private Optional<PassivmitgliedDto> fetch(Condition condition) {
        return fetch(condition, 0, 1).stream().findFirst();
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
                                  passivmitgliedRecord.getStrasseNr(),
                                  passivmitgliedRecord.getPlz(),
                                  passivmitgliedRecord.getOrt(),
                                  passivmitgliedRecord.getCountryCode(),
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
        var newPassivmitglied = new Passivmitglied(null,
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
                                                   UUID.randomUUID().toString(),
                                                   passivmitglied.plz(),
                                                   passivmitglied.strasseNr(),
                                                   passivmitglied.countryCode());
        passivmitgliedDao.insert(newPassivmitglied);

        voucherDao.fetchRangeOfValidUntil(DateUtil.today(), DateUtil.MAX_DATE)
                  .forEach(voucher -> passivmitgliedVoucherDao.insert(new PassivmitgliedVoucher(null,
                                                                                                newPassivmitglied.getId(),
                                                                                                getCode(voucher.getCodePrefix()),
                                                                                                voucher.getDescription(),
                                                                                                voucher.getValidUntil(),
                                                                                                null,
                                                                                                null)));

        mailService.sendSupporterRegistrationEmail(newPassivmitglied);
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
        return fetch(PASSIVMITGLIED.EXTERNAL_ID.eq(externalId))
                .map(passivmitglied -> {
                    var out = new ByteArrayOutputStream();
                    qrBillService.passivmitglied(passivmitglied, PASSIVMITGLIED_AMOUNT, out);
                    return out.toByteArray();
                });
    }

    public Optional<byte[]> qrBillPdf(Long externalId) {
        return fetch(PASSIVMITGLIED.EXTERNAL_ID.eq(externalId))
                .flatMap(passivmitglied -> qrBillService.passivmitgliedPdf(passivmitglied, PASSIVMITGLIED_AMOUNT));
    }

    public Optional<PassivmitgliedDto> findByUUID(String uuid) {
        return fetch(PASSIVMITGLIED.UUID.equal(uuid));
    }

    public byte[] qrCode(Long externalId, PassivmitgliedVoucherDto voucher) {
        try {
            var out = new ByteArrayOutputStream();
            qrCodeService.generate("%s:%s".formatted(externalId, voucher.code()), new MemoryCacheImageOutputStream(out));
            return out.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public void createVouchers(String prefix, String description, LocalDate validUntil) {
        // insert voucher-template for new registrations
        voucherDao.insert(new Voucher(null, prefix, description, validUntil));

        // add for all existing ones
        passivmitgliedDao.findAll()
                         // TODO filter certain (e.g., not payed in last 12 months)?
                         .forEach(passivmitglied -> passivmitgliedVoucherDao.insert(new PassivmitgliedVoucher(null,
                                                                                                              passivmitglied.getId(),
                                                                                                              getCode(prefix),
                                                                                                              description,
                                                                                                              validUntil,
                                                                                                              null,
                                                                                                              null)));
    }

    private static String getCode(String prefix) {
        return "%s-%s".formatted(prefix,
                                 RandomStringUtils.insecure().nextAlphanumeric(5).toLowerCase(ROOT));
    }
}
