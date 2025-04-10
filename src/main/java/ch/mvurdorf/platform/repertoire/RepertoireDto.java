package ch.mvurdorf.platform.repertoire;

import java.time.LocalDateTime;
import java.util.List;

public record RepertoireDto(RepertoireType type,
                            LocalDateTime createdAt,
                            String createdBy,
                            String details,
                            List<RepertoireEntryDto> entries) {

    public List<Long> kompositionIds() {
        return entries.stream()
                      .map(RepertoireEntryDto::kompositionId)
                      .toList();
    }

}
