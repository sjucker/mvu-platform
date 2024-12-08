package ch.mvurdorf.platform.users;

public record UserDto(Long id,
                      String email,
                      String name,
                      boolean active) {
}
