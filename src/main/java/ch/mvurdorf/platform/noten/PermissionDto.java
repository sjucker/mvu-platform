package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Stimme;
import org.jspecify.annotations.Nullable;

public record PermissionDto(Instrument instrument, @Nullable Stimme stimme) {}
