package ch.mvurdorf.platform.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AbsenzState implements LocalizedEnum {
    POSITIVE("anwesend"),
    NEGATIVE("abwesend"),
    INACTIVE("inaktiv"),
    UNKNOWN("?");

    private final String description;

    public static AbsenzState of(String state) {
        if (state == null) {
            return UNKNOWN;
        }
        return valueOf(state);
    }
}
