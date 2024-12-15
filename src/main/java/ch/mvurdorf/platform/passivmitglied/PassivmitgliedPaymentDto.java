package ch.mvurdorf.platform.passivmitglied;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PassivmitgliedPaymentDto(LocalDate datum,
                                       BigDecimal amount,
                                       String bemerkung,
                                       LocalDateTime createdAt,
                                       String createdBy) {
}
