package ch.mvurdorf.platform.repertoire;

import ch.mvurdorf.platform.common.LocalizedEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RepertoireType implements LocalizedEnum {
    KONZERTMAPPE("Konzertmappe"),
    MARSCHBUCH("Marschbuch");

    private final String description;
}
