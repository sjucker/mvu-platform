package ch.mvurdorf.platform.passivmitglied;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PassivmitgliedPaymentDto(LocalDate datum,
                                       BigDecimal amount,
                                       String bemerkung) {
}
