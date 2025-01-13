package ch.mvurdorf.platform.supporter;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SupporterVoucherDto(String code,
                                  String description,
                                  LocalDate validUntil,
                                  LocalDateTime redeemedAt,
                                  String redeemedBy) {
}
