package ch.mvurdorf.platform.supporter;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

public record SupporterDto(Long id,
                           SupporterType type,
                           Long externalId,
                           String uuid,
                           String anrede,
                           String vorname,
                           String nachname,
                           String strasse,
                           String strasseNr,
                           String plz,
                           String ort,
                           String countryCode,
                           String email,
                           String bemerkung,
                           boolean kommunikationPost,
                           boolean kommunikationEmail,
                           LocalDateTime registeredAt,
                           List<SupporterPaymentDto> payments,
                           List<SupporterVoucherDto> vouchers) {

    public Optional<LocalDate> lastPayment() {
        return payments.stream()
                       .sorted(comparing(SupporterPaymentDto::datum).reversed())
                       .map(SupporterPaymentDto::datum)
                       .findFirst();
    }

    public int numberOfPayments() {
        return payments.size();
    }

    public List<SupporterPaymentDto> paymentsNewestFirst() {
        return payments.stream()
                       .sorted(comparing(SupporterPaymentDto::datum).reversed())
                       .toList();
    }

    /**
     * @return the vouchers the supporter can access at the given date (still valid and, if required by the voucher,
     * with a payment in the last n months), newest first
     */
    public List<SupporterVoucherDto> accessibleVouchers(LocalDate today) {
        return vouchers.stream()
                       .filter(voucher -> !today.isAfter(voucher.validUntil()))
                       .filter(voucher -> voucher.paidWithinMonths() == null
                                          || lastPayment().filter(payment -> !payment.isBefore(today.minusMonths(voucher.paidWithinMonths())))
                                                          .isPresent())
                       .sorted(comparing(SupporterVoucherDto::validUntil).reversed())
                       .toList();
    }

    public String fullName() {
        return "%s %s".formatted(vorname, nachname);
    }

    public SupporterEditDto edit() {
        return new SupporterEditDto(id, anrede, vorname, nachname, strasse, strasseNr, plz, ort, email, bemerkung, kommunikationPost, kommunikationEmail);
    }

    public String getStrasseWithNr() {
        return joinNonBlank(strasse, strasseNr);
    }

    public String getPlzOrt() {
        return joinNonBlank(plz, ort);
    }

    private static String joinNonBlank(String... values) {
        return Stream.of(values)
                     .filter(StringUtils::isNotBlank)
                     .collect(joining(" "));
    }
}
