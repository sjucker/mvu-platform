package ch.mvurdorf.platform.supporter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static ch.mvurdorf.platform.utils.FormatUtil.formatCurrency;
import static ch.mvurdorf.platform.utils.FormatUtil.formatDate;

public record SupporterPaymentDto(LocalDate datum,
                                  BigDecimal amount,
                                  String bemerkung,
                                  LocalDateTime createdAt,
                                  String createdBy) {

    public String description() {
        return "%s: %s".formatted(formatDate(datum),
                                  formatCurrency(amount));
    }
}
