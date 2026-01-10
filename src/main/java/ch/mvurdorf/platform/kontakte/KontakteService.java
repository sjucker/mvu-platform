package ch.mvurdorf.platform.kontakte;

import ch.mvurdorf.platform.common.Register;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.List;

import static ch.mvurdorf.platform.jooq.Tables.LOGIN;
import static java.util.Comparator.comparing;

@Service
@RequiredArgsConstructor
public class KontakteService {

    private final DSLContext jooqDsl;

    public List<KontaktDto> findActiveUsers() {
        return jooqDsl.select(LOGIN.NAME, LOGIN.EMAIL, LOGIN.REGISTER)
                      .from(LOGIN)
                      .where(LOGIN.ACTIVE.isTrue())
                      .fetch(it -> new KontaktDto(it.value1(), it.value2(), Register.valueOf(it.value3())))
                      .stream()
                      .sorted(comparing(KontaktDto::name))
                      .toList();
    }
}
