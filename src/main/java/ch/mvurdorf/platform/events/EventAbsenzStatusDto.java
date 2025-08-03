package ch.mvurdorf.platform.events;

public record EventAbsenzStatusDto(Long loginId,
                                   Long eventId,
                                   String title,
                                   String subtitle,
                                   String interna,
                                   AbsenzState status,
                                   String remark) {
}
