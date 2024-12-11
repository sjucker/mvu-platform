package ch.mvurdorf.platform.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public final class DurationUtil {
    private DurationUtil() {
    }

    // expected format: mm:ss
    public static Optional<Integer> toDurationInSeconds(String duration) {
        if (StringUtils.isBlank(duration)) {
            return Optional.empty();
        }

        String[] parts = duration.split(":");
        if (parts.length != 2) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
