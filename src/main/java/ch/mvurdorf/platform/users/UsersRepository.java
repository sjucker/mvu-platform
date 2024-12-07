package ch.mvurdorf.platform.users;

import ch.mvurdorf.platform.jooq.tables.daos.LoginDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Login;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class UsersRepository {

    private final LoginDao loginDao;

    UsersRepository(LoginDao loginDao) {
        this.loginDao = loginDao;
    }

    List<Login> findAll() {
        return loginDao.findAll();
    }
}
