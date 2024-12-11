package ch.mvurdorf.platform.common;

public enum Instrument implements LocalizedEnum {
    FLOETE("Flöte"),
    KLARINETTE("Klarinette"),
    BASS_KLARINETTE("Bass Klarinette"),
    OBOE("Oboe"),
    FAGOTT("Fagott"),
    TROMPETE("Trompete"),
    HORN("Horn"),
    EUPHONIUM("Euphonium"),
    POSAUNE("Posaune"),
    BASSPOSAUNE("Bass Posaune"),
    TUBA("Tuba"),
    PERKUSSION("Perkusion"),
    PARTITUR("Partitur"),
    E_BASS("E-Bass"),
    E_GUITAR("E-Guitar");

    private final String description;

    Instrument(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
