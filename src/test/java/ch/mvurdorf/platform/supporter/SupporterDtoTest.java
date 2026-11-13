package ch.mvurdorf.platform.supporter;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SupporterDtoTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 13);

    private static final SupporterVoucherDto UNRESTRICTED = voucher("ALL", TODAY.plusMonths(1), null);
    private static final SupporterVoucherDto PAID_12_MONTHS = voucher("P12", TODAY.plusMonths(2), 12);
    private static final SupporterVoucherDto PAID_3_MONTHS = voucher("P3", TODAY.plusMonths(3), 3);
    private static final SupporterVoucherDto EXPIRED = voucher("OLD", TODAY.minusDays(1), null);

    private static final List<SupporterVoucherDto> VOUCHERS = List.of(UNRESTRICTED, PAID_12_MONTHS, PAID_3_MONTHS, EXPIRED);

    @Test
    void accessibleVouchersWithoutPayment() {
        assertThat(supporter(List.of()).accessibleVouchers(TODAY)).containsExactly(UNRESTRICTED);
    }

    @Test
    void accessibleVouchersWithRecentPayment() {
        assertThat(supporter(List.of(TODAY.minusMonths(13), TODAY.minusMonths(3))).accessibleVouchers(TODAY))
                .as("newest first, the last payment counts, the boundary day is included")
                .containsExactly(PAID_3_MONTHS, PAID_12_MONTHS, UNRESTRICTED);
    }

    @Test
    void accessibleVouchersWithOldPayment() {
        assertThat(supporter(List.of(TODAY.minusMonths(6))).accessibleVouchers(TODAY)).containsExactly(PAID_12_MONTHS, UNRESTRICTED);
        assertThat(supporter(List.of(TODAY.minusMonths(12).minusDays(1))).accessibleVouchers(TODAY)).containsExactly(UNRESTRICTED);
    }

    private static SupporterVoucherDto voucher(String code, LocalDate validUntil, Integer paidWithinMonths) {
        return new SupporterVoucherDto(code, code, validUntil, null, null, paidWithinMonths);
    }

    private static SupporterDto supporter(List<LocalDate> payments) {
        return new SupporterDto(1L, SupporterType.PASSIVMITGLIED, 1000001L, "uuid", "Lieber", "Max", "Muster",
                                null, null, null, null, null, null, null, false, false, null,
                                payments.stream().map(datum -> new SupporterPaymentDto(datum, BigDecimal.TEN, null, null, null)).toList(),
                                VOUCHERS);
    }
}
