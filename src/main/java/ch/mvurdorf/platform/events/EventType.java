package ch.mvurdorf.platform.events;

import lombok.Getter;

public enum EventType {
    BLUE("Blau (Proben)"),
    RED("Rot (Konzert)"),
    GREEN("Grün (Spezialanlass)"),
    VIOLET("Violett (Spezialprobe)"),
    BLACK("Schwarz fett (Varia wichtig)"),
    GREY("Schwarz (Ferien, Sonstiges)");

    @Getter
    private final String description;

    EventType(String description) {
        this.description = description;
    }
}
