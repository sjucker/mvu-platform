package ch.mvurdorf.platform.users;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Register;

import java.util.Set;

public record UserDto(Long id,
                      String email,
                      String name,
                      boolean active,
                      Register register,
                      Set<Instrument> instrumentPermissions) {
}
