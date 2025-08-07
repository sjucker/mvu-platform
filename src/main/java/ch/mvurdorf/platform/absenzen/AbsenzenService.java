package ch.mvurdorf.platform.absenzen;

import ch.mvurdorf.platform.jooq.tables.daos.AbsenzStatusDao;
import ch.mvurdorf.platform.jooq.tables.daos.LoginDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbsenzenService {

    private final DSLContext jooqDsl;
    private final LoginDao loginDao;
    private final AbsenzStatusDao absenzStatusDao;

    public void findAbsenzenStatusByEvent(Long eventId) {
        // TODO
    }
}
