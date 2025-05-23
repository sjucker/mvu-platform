package ch.mvurdorf.platform.security;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.jooq.tables.daos.InstrumentPermissionDao;
import ch.mvurdorf.platform.jooq.tables.daos.LoginDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Login;
import ch.mvurdorf.platform.security.LoginService.Permission;
import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static ch.mvurdorf.platform.security.LoginService.Permission.READ;
import static ch.mvurdorf.platform.security.LoginService.Permission.WRITE;
import static java.util.stream.Collectors.toSet;

@Component
@RequiredArgsConstructor
public class AuthenticatedUser {

    private final LoginDao loginDao;
    private final InstrumentPermissionDao instrumentPermissionDao;
    private final AuthenticationContext authenticationContext;

    @Transactional
    public Optional<Login> get() {
        return authenticationContext.getAuthenticatedUser(Object.class)
                                    .flatMap(o -> {
                                        if (o instanceof Jwt jwt) {
                                            return Optional.ofNullable(jwt.getClaimAsString("email"));
                                        } else if (o instanceof UserDetails userDetails) {
                                            return Optional.of(userDetails.getUsername());
                                        }
                                        return Optional.empty();
                                    })
                                    .map(loginDao::fetchOneByEmail);
    }

    @Transactional
    public Long getId() {
        return get().map(Login::getId).orElse(0L);
    }

    @Transactional
    public String getName() {
        return get().map(Login::getName).orElse("?");
    }

    @Transactional
    public String getEmail() {
        return get().map(Login::getEmail).orElse("?");
    }

    public boolean hasWritePermission(String group) {
        return hasRole(group, WRITE);
    }

    public boolean hasReadPermission(String group) {
        return hasRole(group, READ) && !hasRole(group, WRITE);
    }

    private boolean hasRole(String group, Permission permission) {
        var role = Permission.getRole(group, permission);
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                                    .map(userDetails -> userDetails.getAuthorities().stream()
                                                                   .anyMatch(auth -> role.equalsIgnoreCase(auth.getAuthority())))
                                    .orElse(false);
    }

    @Transactional
    public Set<Instrument> getInstrumentPermissions() {
        return instrumentPermissionDao.fetchByFkLogin(getId()).stream()
                                      .map(permission -> Instrument.valueOf(permission.getInstrument()))
                                      .collect(toSet());
    }

    public void logout() {
        authenticationContext.logout();
    }

}
