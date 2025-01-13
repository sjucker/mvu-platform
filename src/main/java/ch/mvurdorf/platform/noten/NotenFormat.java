package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.LocalizedEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotenFormat implements LocalizedEnum {
    KONZERTMAPPE("Konzertmappe"),
    MARSCHBUCH("Marschbuch");

    private final String description;
}
