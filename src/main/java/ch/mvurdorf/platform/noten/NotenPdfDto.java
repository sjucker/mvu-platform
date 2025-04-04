package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Stimme;
import ch.mvurdorf.platform.common.Stimmlage;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.StringJoiner;

import static java.util.Locale.ROOT;
import static java.util.stream.Collectors.joining;

public record NotenPdfDto(Long id,
                          @NotNull List<NotenAssignmentDto> assignments,
                          @Nullable Stimmlage stimmlage) implements Comparable<NotenPdfDto> {

    public String description() {
        var joiner = new StringJoiner(", ");
        joiner.add(assignments.stream().map(NotenAssignmentDto::getDescription).collect(joining(", ")));
        if (stimmlage != null) {
            return "%s (in %s)".formatted(joiner, stimmlage.getDescription());
        }
        return joiner.toString();
    }

    public String filename() {
        // TODO
        var joiner = new StringJoiner("-");
        if (assignments.size() == 1) {
            joiner.add(assignments.getFirst().getDescription().toLowerCase(ROOT));
        }
        if (stimmlage != null) {
            joiner.add(stimmlage.name().toLowerCase(ROOT));
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
