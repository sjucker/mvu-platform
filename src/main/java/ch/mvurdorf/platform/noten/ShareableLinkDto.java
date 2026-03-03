package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record ShareableLinkDto(String description,
                               LocalDate validUntil,
                               List<KompositionDto> kompositions,
                               Set<Instrument> instruments) {
}
