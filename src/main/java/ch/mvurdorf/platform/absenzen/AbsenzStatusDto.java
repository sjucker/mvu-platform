package ch.mvurdorf.platform.absenzen;

import ch.mvurdorf.platform.common.AbsenzState;
import ch.mvurdorf.platform.common.Register;

public record AbsenzStatusDto(Long eventId,
                              Long loginId,
                              Register register,
                              String name,
                              String remark,
                              AbsenzState status) {

    public static AbsenzStatusDto of(Register register) {
        return new AbsenzStatusDto(null, null, register, null, null, null);
    }

    public boolean isRegisterPlaceholder() {
        return loginId == null;
    }

}
