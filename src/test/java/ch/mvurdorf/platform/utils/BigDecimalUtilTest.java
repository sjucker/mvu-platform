package ch.mvurdorf.platform.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BigDecimalUtilTest {

    @Test
    void formatBigDecimal() {
        assertThat(BigDecimalUtil.formatBigDecimal(new BigDecimal("12.00"))).isEqualTo("12");
        assertThat(BigDecimalUtil.formatBigDecimal(new BigDecimal("12.1"))).isEqualTo("12.1");
        assertThat(BigDecimalUtil.formatBigDecimal(new BigDecimal("12.09"))).isEqualTo("12.1");
    }
}
