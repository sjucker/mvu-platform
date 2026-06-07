package ch.mvurdorf.platform.common;

import java.util.Optional;

public interface ParsableEnum<T extends Enum<T>> {

    Optional<T> parse(String value);

}
