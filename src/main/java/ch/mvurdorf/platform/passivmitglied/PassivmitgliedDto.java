package ch.mvurdorf.platform.passivmitglied;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;

public record PassivmitgliedDto(Long id,
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
                                List<PassivmitgliedPaymentDto> payments,
                                List<PassivmitgliedVoucherDto> vouchers) {

    public Optional<LocalDate> lastPayment() {
        return payments.stream()
                       .sorted(comparing(PassivmitgliedPaymentDto::datum).reversed())
                       .map(PassivmitgliedPaymentDto::datum)
                       .findFirst();
    }

    public int numberOfPayments() {
        return payments.size();
    }

    public List<PassivmitgliedPaymentDto> paymentsNewestFirst() {
        return payments.stream()
                       .sorted(comparing(PassivmitgliedPaymentDto::datum).reversed())
                       .toList();
    }

    public List<PassivmitgliedVoucherDto> vouchersNewestFirst() {
        return vouchers.stream()
                       .sorted(comparing(PassivmitgliedVoucherDto::validUntil).reversed())
                       .toList();
    }

    public String fullName() {
        return "%s %s".formatted(vorname, nachname);
    }

    public PassivmitgliedEditDTO edit() {
        return new PassivmitgliedEditDTO(id, anrede, vorname, nachname, strasse, strasseNr, plz, ort, email, bemerkung, kommunikationPost, kommunikationEmail);
    }
}
