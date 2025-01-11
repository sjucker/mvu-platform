package ch.mvurdorf.platform.konzerte;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static ch.mvurdorf.platform.utils.FormatUtil.formatDateTime;

public record KonzertDto(Long id,
                         String name,
                         LocalDate datum,
                         LocalTime zeit,
                         String location,
                         String description,
                         List<KonzertEntryDto> entries) {

    public static KonzertDto empty() {
        return new KonzertDto(null, null, null, null, null, null, new ArrayList<>());
    }

    public LocalDateTime datumZeit() {
        return LocalDateTime.of(datum, zeit);
    }

    public String dateTimeAndLocation() {
        return "%s, %s".formatted(formatDateTime(datumZeit()), location());
    }
}
