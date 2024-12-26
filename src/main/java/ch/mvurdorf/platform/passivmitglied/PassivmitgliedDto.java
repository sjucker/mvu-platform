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
                                String ort,
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

    public String fullName() {
        return "%s %s".formatted(vorname, nachname);
    }
}
