package ch.mvurdorf.platform.messaging;

import ch.mvurdorf.platform.jooq.tables.daos.LoginDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingService {

    private final LoginDao loginDao;

    public void updateToken(String email, String token) {
        var login = loginDao.fetchOptionalByEmail(email).orElseThrow(() -> new NoSuchElementException("No login found for email " + email));
        login.setFcmToken(token);
        loginDao.update(login);
        log.info("Updated FCM token for {}", email);
    }
}
