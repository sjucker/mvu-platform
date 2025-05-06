package ch.mvurdorf.platform.security;

import ch.mvurdorf.platform.jooq.tables.daos.LoginDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Login;
import ch.mvurdorf.platform.utils.DateUtil;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LoginService implements UserDetailsService {

    public static final String USERS_GROUP = "USERS";
    public static final String NOTEN_GROUP = "NOTEN";
    public static final String SUPPORTER_GROUP = "SUPPORTER";
    public static final String KONZERTE_GROUP = "KONZERTE";
    public static final String REPERTOIRE_GROUP = "REPERTOIRE";
    public static final String DOCUMENT_GROUP = "DOCUMENT";

    private final LoginDao loginDao;

    public LoginService(LoginDao loginDao) {
        this.loginDao = loginDao;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var login = loginDao.fetchOptionalByEmail(email).orElse(null);
        if (login == null) {
            throw new UsernameNotFoundException("no user present with username: " + email);
        } else {
            login.setLastLogin(DateUtil.now());
            loginDao.update(login);

            var active = login.getActive();
            // just return a password so TokenBasedRememberMeServices sets an actual remember-me cookie
            return new User(login.getEmail(), "not-actual-password", active, active, active, active, getAuthorities(login));
        }
    }

    private static List<GrantedAuthority> getAuthorities(Login login) {
        var roles = new ArrayList<GrantedAuthority>();

        Permission.of(USERS_GROUP, login.getUsersPermission()).ifPresent(roles::addAll);
        Permission.of(NOTEN_GROUP, login.getNotenPermission()).ifPresent(roles::addAll);
        Permission.of(SUPPORTER_GROUP, login.getSupporterPermission()).ifPresent(roles::addAll);
        Permission.of(KONZERTE_GROUP, login.getKonzertePermission()).ifPresent(roles::addAll);
        Permission.of(REPERTOIRE_GROUP, login.getRepertoirePermission()).ifPresent(roles::addAll);
        Permission.of(DOCUMENT_GROUP, login.getDocumentPermission()).ifPresent(roles::addAll);

        return roles;
    }

    public enum Permission {
        NONE,
        READ,
        WRITE;

        public static Optional<List<GrantedAuthority>> of(String group, String permission) {
            return switch (Permission.valueOf(permission)) {
                case NONE -> Optional.empty();
                case READ -> Optional.of(List.of(new SimpleGrantedAuthority(getRole(group, READ))));
                case WRITE -> Optional.of(List.of(new SimpleGrantedAuthority(getRole(group, READ)),
                                                  new SimpleGrantedAuthority(getRole(group, WRITE))));
            };
        }

        public static String getRole(String group, Permission permission) {
            return "ROLE_" + group + (permission == WRITE ? "_WRITE" : "");
        }
    }

}
