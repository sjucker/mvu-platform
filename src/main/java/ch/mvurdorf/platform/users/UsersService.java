package ch.mvurdorf.platform.users;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.jooq.tables.daos.InstrumentPermissionDao;
import ch.mvurdorf.platform.jooq.tables.daos.LoginDao;
import ch.mvurdorf.platform.jooq.tables.pojos.InstrumentPermission;
import ch.mvurdorf.platform.jooq.tables.pojos.Login;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.jooq.DSLContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ch.mvurdorf.platform.jooq.Tables.INSTRUMENT_PERMISSION;
import static ch.mvurdorf.platform.jooq.Tables.LOGIN;
import static ch.mvurdorf.platform.security.LoginService.Permission.NONE;
import static java.util.Comparator.comparing;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.delete;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

@RequiredArgsConstructor
@Service
class UsersService {

    private final DSLContext jooqDsl;
    private final LoginDao loginDao;
    private final InstrumentPermissionDao instrumentPermissionDao;
    private final PasswordEncoder passwordEncoder;

    List<UserDto> findAll() {
        return jooqDsl.select(LOGIN,
                              multiset(
                                      select(INSTRUMENT_PERMISSION.INSTRUMENT)
                                              .from(INSTRUMENT_PERMISSION)
                                              .where(INSTRUMENT_PERMISSION.FK_LOGIN.eq(LOGIN.ID))
                              ).convertFrom(it -> it.map(mapping(value -> Instrument.valueOf(value)))))
                      .from(LOGIN)
                      .fetch(it -> {
                          var login = it.value1();
                          return new UserDto(login.getId(), login.getEmail(), login.getName(), login.getActive(), new HashSet<>(it.value2()));
                      })
                      .stream()
                      .sorted(comparing(UserDto::name))
                      .toList();
    }

    public void update(UserDto user) {
        var login = loginDao.findOptionalById(user.id()).orElseThrow();
        login.setEmail(user.email());
        login.setName(user.name());
        loginDao.update(login);

        jooqDsl.execute(delete(INSTRUMENT_PERMISSION)
                                .where(INSTRUMENT_PERMISSION.FK_LOGIN.eq(login.getId())));

        insertInstrumentPermissions(user.instrumentPermissions(), login.getId());
    }

    private void insertInstrumentPermissions(Set<Instrument> instruments, Long loginId) {
        instruments.stream()
                   .map(it -> new InstrumentPermission(loginId, it.name()))
                   .forEach(instrumentPermissionDao::insert);
    }

    public String create(UserDto newUser) {
        var password = RandomStringUtils.secure().nextAlphanumeric(8);
        var newLogin = new Login(null,
                                 newUser.email(),
                                 newUser.name(),
                                 passwordEncoder.encode(password),
                                 true,
                                 null,
                                 NONE.name(),
                                 NONE.name(),
                                 NONE.name(),
                                 NONE.name(),
                                 NONE.name());
        loginDao.insert(newLogin);

        insertInstrumentPermissions(newUser.instrumentPermissions(), newLogin.getId());

        return password;
    }
}
