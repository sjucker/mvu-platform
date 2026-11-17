package ch.mvurdorf.platform.passkey;

import java.time.LocalDateTime;

public record PasskeyDto(Long id, String name, LocalDateTime createdAt, LocalDateTime lastUsedAt) {
}
