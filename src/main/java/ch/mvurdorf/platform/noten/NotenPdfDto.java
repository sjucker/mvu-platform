package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Notenschluessel;
import ch.mvurdorf.platform.common.Stimme;
import ch.mvurdorf.platform.common.Stimmlage;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public record NotenPdfDto(Long id,
                          String kompositionTitel,
                          @NotNull List<NotenAssignmentDto> assignments,
                          @Nullable Stimmlage stimmlage,
                          @Nullable Notenschluessel notenschluessel) implements Comparable<NotenPdfDto> {

    public String description() {
        var assignmentsByInstrument = assignments.stream()
                                                 .collect(groupingBy(NotenAssignmentDto::instrument, toList()));

        var joiner = new StringJoiner(", ");
        assignmentsByInstrument.keySet().stream()
                               .sorted()
                               .forEach(instrument -> {
                                   var dtos = assignmentsByInstrument.get(instrument);
                                   if (dtos.size() > 1) {
                                       var stimmen = dtos.stream()
                                                         .map(NotenAssignmentDto::stimme)
                                                         // at this point, the stimme cannot be null, otherwise we could not have multiple entries for the same instrument. just to make the linter happy
                                                         .filter(Objects::nonNull)
                                                         .sorted()
                                                         .map(Stimme::getDescription)
                                                         .collect(joining("/"));
                                       joiner.add("%s %s".formatted(instrument.getDescription(), stimmen));
                                   } else {
                                       joiner.add(dtos.getFirst().getDescription());
                                   }
                               });

        if (stimmlage != null && notenschluessel != null) {
            return "%s (in %s, %s)".formatted(joiner, stimmlage.getDescription(), notenschluessel.getDescription());
        } else if (stimmlage != null) {
            return "%s (in %s)".formatted(joiner, stimmlage.getDescription());
        } else if (notenschluessel != null) {
            return "%s (%s)".formatted(joiner, notenschluessel.getDescription());
        } else {
            return joiner.toString();
        }
    }

    public String filename() {
        var joiner = new StringJoiner(" ");
        joiner.add(kompositionTitel);
        if (assignments.size() == 1) {
            joiner.add("(" + assignments.getFirst().getDescription() + ")");
        }
        return joiner + ".pdf";
    }

    public List<String> assignmentDescriptions() {
        return assignments.stream()
                          .map(NotenAssignmentDto::getDescription)
                          .toList();
    }

    public boolean allowed(Instrument instrument) {
        return assignments.stream().anyMatch(assignment -> assignment.instrument() == instrument);
    }

    public boolean allowed(Instrument instrument, Stimme stimme) {
        return assignments.stream().anyMatch(assignment -> assignment.instrument() == instrument && (assignment.stimme() == null || assignment.stimme() == stimme));
    }

    @Override
    public int compareTo(NotenPdfDto other) {
        return assignments().getFirst().instrument().compareTo(other.assignments().getFirst().instrument());
    }
}
