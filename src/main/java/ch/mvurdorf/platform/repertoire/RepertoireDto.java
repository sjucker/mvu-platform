package ch.mvurdorf.platform.repertoire;

import java.time.LocalDateTime;
import java.util.List;

public record RepertoireDto(RepertoireType type,
                            LocalDateTime createdAt,
                            String details,
                            List<RepertoireEntryDto> entries) {
}
