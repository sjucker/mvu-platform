package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.jooq.tables.daos.NotenDao;
import org.springframework.stereotype.Service;

@Service
public class NotenService {

    private final NotenDao notenDao;

    public NotenService(NotenDao notenDao) {
        this.notenDao = notenDao;
    }
}
