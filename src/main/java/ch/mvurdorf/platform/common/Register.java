package ch.mvurdorf.platform.common;

import lombok.Getter;

public enum Register {
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

    @Getter
    private final String description;

    Register(String description) {
        this.description = description;
    }
}
