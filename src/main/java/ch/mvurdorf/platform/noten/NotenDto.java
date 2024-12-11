package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Stimme;
import ch.mvurdorf.platform.common.Stimmlage;

public record NotenDto(Instrument instrument,
                       Stimme stimme,
                       Stimmlage stimmlage) {
}
