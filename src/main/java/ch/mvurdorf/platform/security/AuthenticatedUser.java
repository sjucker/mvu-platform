package ch.mvurdorf.platform.security;

import ch.mvurdorf.platform.jooq.tables.daos.LoginDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Login;
import ch.mvurdorf.platform.security.LoginService.Permission;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static ch.mvurdorf.platform.security.LoginService.Permission.READ;
import static ch.mvurdorf.platform.security.LoginService.Permission.WRITE;

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
                                    .map(userDetails -> loginDao.fetchOneByEmail(userDetails.getUsername()));
    }

    public boolean hasWritePermission(String group) {
        return hasRole(group, WRITE);
    }

    public boolean hasReadPermission(String group) {
        return hasRole(group, READ);
    }

    private boolean hasRole(String group, Permission permission) {
        var role = Permission.getRole(group, permission);
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                                    .map(userDetails -> userDetails.getAuthorities().stream()
                                                                   .anyMatch(auth -> role.equalsIgnoreCase(auth.getAuthority())))
                                    .orElse(false);
    }

    public void logout() {
        authenticationContext.logout();
    }

}
