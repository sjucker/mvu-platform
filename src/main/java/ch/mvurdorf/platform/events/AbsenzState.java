package ch.mvurdorf.platform.events;

public enum AbsenzState {
    POSITIVE, NEGATIVE, INACTIVE, UNKNOWN;

    public static AbsenzState of(String state) {
        if (state == null) {
            return UNKNOWN;
        }
        return valueOf(state);
    }
}
