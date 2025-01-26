package ch.mvurdorf.platform.users;

import ch.mvurdorf.platform.common.Instrument;

import java.util.Set;

public record UserDto(Long id,
                      String email,
                      String name,
                      boolean active,
                      Set<Instrument> instrumentPermissions) {
}
