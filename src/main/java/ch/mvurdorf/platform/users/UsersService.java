package ch.mvurdorf.platform.users;

import ch.mvurdorf.platform.jooq.tables.daos.LoginDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Login;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static ch.mvurdorf.platform.security.LoginService.Permission.NONE;
import static java.util.Comparator.comparing;

@Service
class UsersService {

    private final LoginDao loginDao;
    private final PasswordEncoder passwordEncoder;

    UsersService(LoginDao loginDao, PasswordEncoder passwordEncoder) {
        this.loginDao = loginDao;
        this.passwordEncoder = passwordEncoder;
    }

    List<UserDto> findAll() {
        return loginDao.findAll().stream()
                       .map(l -> new UserDto(l.getId(), l.getEmail(), l.getName(), l.getActive()))
                       .sorted(comparing(UserDto::name))
                       .toList();
    }

    public void update(UserDto user) {
        var login = loginDao.findOptionalById(user.id()).orElseThrow();
        login.setEmail(user.email());
        login.setName(user.name());
        loginDao.update(login);
    }

    public String create(UserDto newUser) {
        var password = RandomStringUtils.secure().nextAlphanumeric(8);
        loginDao.insert(new Login(null,
                                  newUser.email(),
                                  newUser.name(),
                                  passwordEncoder.encode(password),
                                  true,
                                  null,
                                  NONE.name(),
                                  NONE.name(),
                                  NONE.name()));
        return password;
    }
}
