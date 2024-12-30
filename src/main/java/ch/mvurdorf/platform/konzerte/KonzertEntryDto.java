package ch.mvurdorf.platform.konzerte;

public record KonzertEntryDto(String description,
                              Integer index,
                              Long kompositionId,
                              String kompositionTitel,
                              String kompositionArrangeur) {

    public String titel() {
        if (kompositionId != null) {
            return kompositionTitel + " " + kompositionArrangeur;
        } else {
            return description;
        }
    }
}
