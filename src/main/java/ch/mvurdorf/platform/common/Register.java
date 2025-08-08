package ch.mvurdorf.platform.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Register implements LocalizedEnum {
    FLOETEN("Flöten"),
    KLARINETTEN("Klarinetten"),
    SAXOPHONE("Saxophone"),
    EUPHONIEN("Euphonien"),
    HOERNER("Hörner"),
    TROMPETEN("Trompeten"),
    POSAUNEN("Posaunen"),
    BAESSE("Bässe"),
    SCHLAGWERK("Schlagwerk"),
    DIRIGENT("Dirigent");

    private final String description;
}
