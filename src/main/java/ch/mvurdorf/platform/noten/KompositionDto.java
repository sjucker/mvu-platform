package ch.mvurdorf.platform.noten;

public record KompositionDto(String titel,
                             String komponist,
                             String arrangeur,
                             Integer durationInSeconds) {
}
