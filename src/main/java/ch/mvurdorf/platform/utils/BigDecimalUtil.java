package ch.mvurdorf.platform.utils;

import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class BigDecimalUtil {

    public static String formatBigDecimal(BigDecimal number) {
        var df = new DecimalFormat("#.#");
        return df.format(number);
    }

}
