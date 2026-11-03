package ch.mvurdorf.platform.supporter;

import ch.mvurdorf.platform.common.LocalizedEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupporterMailingChannel implements LocalizedEnum {
    EMAIL("E-Mail"),
    POST("Brief");

    private final String description;
}
