package ch.mvurdorf.platform.security;

import ch.mvurdorf.platform.jooq.tables.daos.LoginDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Login;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class AuthenticatedUser {

    private final LoginDao loginDao;
    private final AuthenticationContext authenticationContext;

    public AuthenticatedUser(AuthenticationContext authenticationContext, LoginDao loginDao) {
        this.loginDao = loginDao;
        this.authenticationContext = authenticationContext;
    }

    @Transactional
    public Optional<Login> get() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                                    .map(userDetails -> loginDao.findById(userDetails.getUsername()));
    }

    public void logout() {
        authenticationContext.logout();
    }

}
