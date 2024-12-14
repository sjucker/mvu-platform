package ch.mvurdorf.platform.passivmitglied;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;

public record PassivmitgliedDto(String vorname,
                                String nachname,
                                String strasse,
                                String ort,
                                String email,
                                String bemerkung,
                                boolean kommunikationPost,
                                boolean kommunikationEmail,
                                LocalDateTime registeredAt,
                                List<PassivmitgliedPaymentDto> payments) {

    public Optional<LocalDate> lastPayment() {
        return payments.stream()
                       .sorted(comparing(PassivmitgliedPaymentDto::datum).reversed())
                       .map(PassivmitgliedPaymentDto::datum)
                       .findFirst();
    }

    public int numberOfPayments() {
        return payments.size();
    }
}
