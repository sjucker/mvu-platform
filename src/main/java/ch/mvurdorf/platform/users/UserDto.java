package ch.mvurdorf.platform.users;

public record UserDto(String email,
                      String name,
                      boolean active) {
}
