package ch.mvurdorf.platform.absenzen;

import ch.mvurdorf.platform.events.AbsenzState;

public record AbsenzStatusDto(Long eventId,
                              Long loginId,
                              // TODO
                              String remark,
                              AbsenzState status) {
}
