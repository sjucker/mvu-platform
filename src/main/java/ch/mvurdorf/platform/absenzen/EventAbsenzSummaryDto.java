package ch.mvurdorf.platform.absenzen;

import java.time.LocalDate;

import static ch.mvurdorf.platform.utils.FormatUtil.formatDate;

public record EventAbsenzSummaryDto(Long id,
                                    LocalDate fromDate,
                                    String title,
                                    int totalPositive,
                                    int totalNegative) {

    public String getTitleWithDate() {
        return "%s vom %s".formatted(title, formatDate(fromDate));
    }
}
