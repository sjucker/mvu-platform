package ch.mvurdorf.platform.noten;

public record KompositionDto(Long id,
                             String titel,
                             String komponist,
                             String arrangeur,
                             Integer durationInSeconds) {
}
