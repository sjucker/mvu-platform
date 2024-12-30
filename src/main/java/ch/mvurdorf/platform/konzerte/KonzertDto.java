package ch.mvurdorf.platform.konzerte;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record KonzertDto(String name,
                         LocalDate datum,
                         LocalTime zeit,
                         String location,
                         String description,
                         List<KonzertEntryDto> entries) {

    public LocalDateTime datumZeit() {
        return LocalDateTime.of(datum, zeit);
    }
}
