package ch.mvurdorf.platform.passivmitglied;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PassivmitgliedVoucherDto(String code,
                                       String description,
                                       LocalDate validUntil,
                                       LocalDateTime redeemedAt,
                                       String redeemedBy) {
}
