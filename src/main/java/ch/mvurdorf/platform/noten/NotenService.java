package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.jooq.tables.daos.NotenDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Noten;
import ch.mvurdorf.platform.service.StorageService;
import org.springframework.stereotype.Service;

import static java.lang.String.valueOf;

@Service
public class NotenService {

    private final NotenDao notenDao;
    private final StorageService storageService;

    public NotenService(NotenDao notenDao, StorageService storageService) {
        this.notenDao = notenDao;
        this.storageService = storageService;
    }

    public void insert(Long kompositionId, NotenDto noten, byte[] file) {
        var notenPojo = new Noten(null, kompositionId, noten.instrument().name(), noten.stimme().name(), noten.stimmlage().name());
        notenDao.insert(notenPojo);
        storageService.write(valueOf(notenPojo.getId()), file);
    }

}
