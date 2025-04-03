package ch.mvurdorf.platform.supporter;

import ch.mvurdorf.platform.jooq.tables.daos.SupporterDao;
import ch.mvurdorf.platform.jooq.tables.daos.SupporterPaymentDao;
import ch.mvurdorf.platform.jooq.tables.daos.SupporterVoucherDao;
import ch.mvurdorf.platform.jooq.tables.daos.VoucherDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Supporter;
import ch.mvurdorf.platform.jooq.tables.pojos.SupporterPayment;
import ch.mvurdorf.platform.jooq.tables.pojos.SupporterVoucher;
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

import static ch.mvurdorf.platform.jooq.Tables.SUPPORTER;
import static ch.mvurdorf.platform.jooq.Tables.SUPPORTER_PAYMENT;
import static ch.mvurdorf.platform.jooq.Tables.SUPPORTER_VOUCHER;
import static java.util.Locale.ROOT;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.math.NumberUtils.toLong;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

@Service
@RequiredArgsConstructor
public class SupporterService {

    private final DSLContext jooqDsl;
    private final SupporterDao supporterDao;
    private final VoucherDao voucherDao;
    private final SupporterPaymentDao supporterPaymentDao;
    private final SupporterVoucherDao supporterVoucherDao;
    private final QRBillService qrBillService;
    private final QRCodeService qrCodeService;
    private final MailService mailService;

    public ConfigurableFilterDataProvider<SupporterDto, Void, String> dataProvider() {
        var dataProvider = DataProvider.<SupporterDto, String>fromFilteringCallbacks(
                query -> fetch(query.getFilter().orElse(null), query.getOffset(), query.getLimit()),
                query -> count(query.getFilter().orElse(null))
        );
        return dataProvider.withConfigurableFilter();
    }

    private Stream<SupporterDto> fetch(String filter, int offset, int limit) {
        return fetch(filterCondition(filter), offset, limit).stream();
    }

    private Optional<SupporterDto> fetch(Condition condition) {
        return fetch(condition, 0, 1).stream().findFirst();
    }

    private List<SupporterDto> fetch(Condition condition, int offset, int limit) {
        return jooqDsl.select(
                              SUPPORTER,
                              multiset(
                                      select(SUPPORTER_PAYMENT.DATUM,
                                             SUPPORTER_PAYMENT.AMOUNT,
                                             SUPPORTER_PAYMENT.BEMERKUNG,
                                             SUPPORTER_PAYMENT.CREATED_AT,
                                             SUPPORTER_PAYMENT.CREATED_BY)
                                              .from(SUPPORTER_PAYMENT)
                                              .where(SUPPORTER_PAYMENT.FK_SUPPORTER.eq(SUPPORTER.ID))
                                              .orderBy(SUPPORTER_PAYMENT.DATUM.desc())
                              ).convertFrom(it -> it.map(mapping(SupporterPaymentDto::new))),
                              multiset(
                                      select(SUPPORTER_VOUCHER.CODE,
                                             SUPPORTER_VOUCHER.DESCRIPTION,
                                             SUPPORTER_VOUCHER.VALID_UNTIL,
                                             SUPPORTER_VOUCHER.REDEEMED_AT,
                                             SUPPORTER_VOUCHER.REDEEMED_BY)
                                              .from(SUPPORTER_VOUCHER)
                                              .where(SUPPORTER_VOUCHER.FK_SUPPORTER.eq(SUPPORTER.ID))
                                              .orderBy(SUPPORTER_VOUCHER.VALID_UNTIL.desc())
                              ).convertFrom(it -> it.map(mapping(SupporterVoucherDto::new)))
                      )
                      .from(SUPPORTER)
                      .where(condition)
                      .offset(offset)
                      .limit(limit)
                      .fetch(it -> {
                          var supporterRecord = it.value1();
                          return new SupporterDto(
                                  supporterRecord.getId(),
                                  SupporterType.valueOf(supporterRecord.getType()),
                                  supporterRecord.getExternalId(),
                                  supporterRecord.getUuid(),
                                  supporterRecord.getAnrede(),
                                  supporterRecord.getVorname(),
                                  supporterRecord.getNachname(),
                                  supporterRecord.getStrasse(),
                                  supporterRecord.getStrasseNr(),
                                  supporterRecord.getPlz(),
                                  supporterRecord.getOrt(),
                                  supporterRecord.getCountryCode(),
                                  supporterRecord.getEmail(),
                                  supporterRecord.getBemerkung(),
                                  supporterRecord.getKommunikationPost(),
                                  supporterRecord.getKommunikationEmail(),
                                  supporterRecord.getRegisteredAt(),
                                  it.value2(),
                                  it.value3()
                          );
                      });
    }

    private int count(String filter) {
        return jooqDsl.fetchCount(SUPPORTER, filterCondition(filter));

    }

    private static Condition filterCondition(String filter) {
        if (isBlank(filter)) {
            return DSL.trueCondition();
        }

        var filterAsLong = toLong(filter);
        return DSL.or(
                SUPPORTER.VORNAME.containsIgnoreCase(filter),
                SUPPORTER.NACHNAME.containsIgnoreCase(filter),
                SUPPORTER.EMAIL.containsIgnoreCase(filter),
                filterAsLong > 0 ? SUPPORTER.EXTERNAL_ID.contains(filterAsLong) : DSL.noCondition()
        );
    }

    public boolean exists(String email) {
        return supporterDao.fetchOptionalByEmail(email).isPresent();
    }

    public long create(SupporterDto supporter) {
        var externalId = getNextExternalId();
        var newSupporter = new Supporter(null,
                                         supporter.vorname(),
                                         supporter.nachname(),
                                         supporter.strasse(),
                                         supporter.ort(),
                                         supporter.email(),
                                         supporter.bemerkung(),
                                         supporter.kommunikationPost(),
                                         supporter.kommunikationEmail(),
                                         DateUtil.now(),
                                         externalId,
                                         supporter.anrede(),
                                         UUID.randomUUID().toString(),
                                         supporter.plz(),
                                         supporter.strasseNr(),
                                         supporter.countryCode(),
                                         supporter.type().name());
        supporterDao.insert(newSupporter);

        voucherDao.fetchRangeOfValidUntil(DateUtil.today(), DateUtil.MAX_DATE).stream()
                  .filter(voucher -> SupporterType.valueOf(voucher.getType()) == supporter.type())
                  .forEach(voucher -> supporterVoucherDao.insert(new SupporterVoucher(null,
                                                                                      newSupporter.getId(),
                                                                                      getCode(voucher.getCodePrefix()),
                                                                                      voucher.getDescription(),
                                                                                      voucher.getValidUntil(),
                                                                                      null,
                                                                                      null)));

        mailService.sendSupporterRegistrationEmail(newSupporter);
        return externalId;
    }

    public void update(SupporterEditDto supporter) {
        var existing = supporterDao.findOptionalById(supporter.getId()).orElseThrow();
        supporter.applyTo(existing);
        supporterDao.update(existing);
    }

    private long getNextExternalId() {
        return jooqDsl.select(DSL.max(SUPPORTER.EXTERNAL_ID))
                      .from(SUPPORTER)
                      .fetchOptional(Record1::value1)
                      .map(it -> it + 1L)
                      .orElse(1000001L);
    }

    public void addPayments(Long supporterId, List<SupporterPaymentDto> payments, String user) {
        payments.forEach(payment -> supporterPaymentDao.insert(new SupporterPayment(null,
                                                                                    supporterId,
                                                                                    payment.datum(),
                                                                                    payment.amount(),
                                                                                    payment.bemerkung(),
                                                                                    DateUtil.now(),
                                                                                    user)));
    }

    public Optional<byte[]> qrBill(Long externalId) {
        return fetch(SUPPORTER.EXTERNAL_ID.eq(externalId))
                .map(supporter -> {
                    var out = new ByteArrayOutputStream();
                    qrBillService.supporter(supporter, supporter.type().getAmount(), out);
                    return out.toByteArray();
                });
    }

    public Optional<byte[]> qrBillPdf(Long externalId) {
        return fetch(SUPPORTER.EXTERNAL_ID.eq(externalId))
                .flatMap(supporter -> qrBillService.supporterPdf(supporter, supporter.type().getAmount()));
    }

    public Optional<SupporterDto> findByUUID(String uuid) {
        return fetch(SUPPORTER.UUID.equal(uuid));
    }

    public byte[] qrCode(Long externalId, SupporterVoucherDto voucher) {
        try {
            var out = new ByteArrayOutputStream();
            qrCodeService.generate("%s:%s".formatted(externalId, voucher.code()), new MemoryCacheImageOutputStream(out));
            return out.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public void createVouchers(String prefix, String description, LocalDate validUntil, SupporterType type) {
        // insert voucher-template for new registrations
        voucherDao.insert(new Voucher(null, prefix, description, validUntil, type.name()));

        // add for all existing ones
        supporterDao.findAll().stream()
                    .filter(supporter -> SupporterType.valueOf(supporter.getType()) == type)
                    // TODO filter certain (e.g., not payed in last 12 months)?
                    .forEach(supporter -> supporterVoucherDao.insert(new SupporterVoucher(null,
                                                                                          supporter.getId(),
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
