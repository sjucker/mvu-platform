package ch.mvurdorf.platform.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DurationUtilTest {

    @Test
    void toDurationInSeconds(){
        assertThat(DurationUtil.toDurationInSeconds(null)).isEmpty();
        assertThat(DurationUtil.toDurationInSeconds("")).isEmpty();
        assertThat(DurationUtil.toDurationInSeconds("1")).isEmpty();
        assertThat(DurationUtil.toDurationInSeconds("abc")).isEmpty();
        assertThat(DurationUtil.toDurationInSeconds("ab:cd")).isEmpty();

        assertThat(DurationUtil.toDurationInSeconds("00:00")).hasValue(0);
        assertThat(DurationUtil.toDurationInSeconds("00:01")).hasValue(1);
        assertThat(DurationUtil.toDurationInSeconds("12:06")).hasValue(726);
    }
}
