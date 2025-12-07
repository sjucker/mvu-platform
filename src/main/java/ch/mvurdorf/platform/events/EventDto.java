package ch.mvurdorf.platform.events;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record EventDto(Long id,
                       LocalDate fromDate,
                       LocalTime fromTime,
                       LocalDate toDate,
                       LocalTime toTime,
                       boolean approximately,
                       String title,
                       String description,
                       String location,
                       String interna,
                       String literature,
                       EventType type,
                       boolean infoOnly,
                       boolean relevantForAbsenz,
                       boolean relevantForWebsite,
                       LocalDateTime createdAt,
                       String createdBy,
                       LocalDateTime updatedAt,
                       String updatedBy) {

    public LocalDateTime getLastUpdatedAt() {
        return updatedAt != null ? updatedAt : createdAt;
    }

    public String getLastUpdatedBy() {
        return updatedBy != null ? updatedBy : createdBy;
    }
}
