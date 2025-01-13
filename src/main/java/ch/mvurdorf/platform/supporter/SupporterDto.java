package ch.mvurdorf.platform.supporter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;

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

    public List<SupporterVoucherDto> vouchersNewestFirst() {
        return vouchers.stream()
                       .sorted(comparing(SupporterVoucherDto::validUntil).reversed())
                       .toList();
    }

    public String fullName() {
        return "%s %s".formatted(vorname, nachname);
    }

    public SupporterEditDTO edit() {
        return new SupporterEditDTO(id, anrede, vorname, nachname, strasse, strasseNr, plz, ort, email, bemerkung, kommunikationPost, kommunikationEmail);
    }
}
