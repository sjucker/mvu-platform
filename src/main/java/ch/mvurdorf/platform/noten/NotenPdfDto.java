package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Stimme;
import ch.mvurdorf.platform.common.Stimmlage;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import static java.util.Locale.ROOT;
import static java.util.stream.Collectors.joining;

public record NotenPdfDto(Long id,
                          @NotNull Set<NotenAssignmentDto> assignments,
                          @Nullable Stimmlage stimmlage) {

    public String description() {
        var joiner = new StringJoiner(", ");
        joiner.add(assignments.stream().map(NotenAssignmentDto::getDescription).collect(joining(", ")));
        if (stimmlage != null) {
            joiner.add(stimmlage.getDescription());
        }
        return joiner.toString();
    }

    public String filename() {
        var joiner = new StringJoiner("-");
        if (assignments.size() == 1) {
            // TODO
            joiner.add(assignments.iterator().next().getDescription().toLowerCase(ROOT));
        }
        // TODO else?
        if (stimmlage != null) {
            joiner.add(stimmlage.name().toLowerCase(ROOT));
        }
        // TODO stimme
        return joiner + ".pdf";
    }

    public List<String> assignmentDescriptions() {
        return assignments.stream()
                          .map(NotenAssignmentDto::getDescription)
                          .toList();
    }

    public boolean applies(Instrument instrument) {
        return assignments.stream().anyMatch(assignment -> assignment.instrument() == instrument);
    }

    public boolean applies(Instrument instrument, Stimme stimme) {
        return assignments.stream().anyMatch(assignment -> assignment.instrument() == instrument && (assignment.stimme() == null || assignment.stimme() == stimme));
    }

}
