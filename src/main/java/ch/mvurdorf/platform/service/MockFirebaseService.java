package ch.mvurdorf.platform.service;

import ch.mvurdorf.platform.users.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Profile("local")
@Service
public class MockFirebaseService implements BaseFirebaseService {
    @Override
    public boolean createUser(String email, String name, String password) {
        log.info("Creating user {} ({})", email, name);
        return true;
    }

    @Override
    public void updateUser(String oldEmail, UserDto user) {
        log.info("Updating user {} to {}", oldEmail, user);
    }

    @Override
    public boolean verifyUsernamePassword(String email, String password) {
        return true;
    }

    @Override
    public boolean changePassword(String email, String currentPassword, String newPassword) {
        log.info("Changing password for {}", email);
        return true;
    }
}
