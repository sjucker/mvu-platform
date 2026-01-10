package ch.mvurdorf.platform.service;

import ch.mvurdorf.platform.users.UserDto;
import com.google.firebase.auth.FirebaseAuthException;

public interface BaseFirebaseService {

    boolean createUser(String email, String name, String password);

    void updateUser(String oldEmail, UserDto user) throws FirebaseAuthException;

    boolean verifyUsernamePassword(String email, String password);

    boolean changePassword(String email, String currentPassword, String newPassword);
}
