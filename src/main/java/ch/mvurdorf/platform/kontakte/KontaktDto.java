package ch.mvurdorf.platform.kontakte;

import ch.mvurdorf.platform.common.Register;

public record KontaktDto(String name, String email, Register register) {
}
