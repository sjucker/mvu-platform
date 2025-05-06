package ch.mvurdorf.platform.document;

import java.time.LocalDateTime;

public record DocumentDto(
    Long id,
    String name,
    String fileType,
    LocalDateTime uploadDate,
    String description
) {
}
