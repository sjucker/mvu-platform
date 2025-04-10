package ch.mvurdorf.platform.supporter;

import ch.mvurdorf.platform.common.LocalizedEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupporterType implements LocalizedEnum {
    PASSIVMITGLIED(20.0, "Passivmitglied"),
    GOENNER(100.0, "Gönner");

    private final double amount;
    private final String description;

}
