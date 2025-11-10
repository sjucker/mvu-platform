package ch.mvurdorf.platform.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Instrument implements LocalizedEnum {
    PICOLLO("Picollo"),
    FLOETE("Flöte"),
    OBOE("Oboe"),
    KLARINETTE("Klarinette"),
    ALT_KLARINETTE("Altklarinette"),
    BASS_KLARINETTE("Bassklarinette"),
    FAGOTT("Fagott"),
    SOPRAN_SAXOPHON("Sopransaxophon"),
    ALT_SAXOPHON("Altsaxophon"),
    TENOR_SAXOPHON("Tenorsaxophon"),
    BARITON_SAXOPHON("Baritonsaxophon"),
    TROMPETE("Trompete"),
    CORNET("Cornet"),
    FLUEGEL_HORN("Flügelhorn"),
    HORN("Horn"),
    TENORHORN("Tenorhorn"),
    EUPHONIUM("Euphonium"),
    POSAUNE("Posaune"),
    BASSPOSAUNE("Bass Posaune"),
    TUBA("Tuba"),
    E_BASS("E-Bass"),
    E_GUITAR("E-Guitar"),
    PIANO("Piano/Keyboard"),
    PERKUSSION("Perkusion"),
    PARTITUR("Partitur");

    private final String description;

}
