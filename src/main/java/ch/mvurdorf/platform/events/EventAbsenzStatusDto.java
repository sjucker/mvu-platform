package ch.mvurdorf.platform.events;

import ch.mvurdorf.platform.common.AbsenzState;

public record EventAbsenzStatusDto(Long loginId,
                                   Long eventId,
                                   String title,
                                   String subtitle,
                                   String interna,
                                   AbsenzState status,
                                   String remark) {
}
