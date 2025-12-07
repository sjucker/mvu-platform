package ch.mvurdorf.platform.events;

import ch.mvurdorf.platform.common.AbsenzState;

import java.time.LocalDateTime;

public record EventAbsenzStatusDto(Long loginId,
                                   Long eventId,
                                   String title,
                                   String subtitle,
                                   String interna,
                                   AbsenzState status,
                                   String remark,
                                   String simpleTitle,
                                   String location,
                                   LocalDateTime from,
                                   LocalDateTime to,
                                   boolean infoOnly) {
}
