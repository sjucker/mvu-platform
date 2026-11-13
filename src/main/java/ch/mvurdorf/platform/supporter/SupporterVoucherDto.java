package ch.mvurdorf.platform.supporter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @param paidWithinMonths the voucher is only accessible with a payment in the last n months (null = no restriction)
 */
public record SupporterVoucherDto(String code,
                                  String description,
                                  LocalDate validUntil,
                                  LocalDateTime redeemedAt,
                                  String redeemedBy,
                                  Integer paidWithinMonths) {
}
