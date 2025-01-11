package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Stimme;
import ch.mvurdorf.platform.common.Stimmlage;
import ch.mvurdorf.platform.jooq.tables.daos.NotenDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Noten;
import ch.mvurdorf.platform.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class NotenService {

    private final NotenDao notenDao;
    private final StorageService storageService;

    public void insert(Long kompositionId, NotenDto noten, byte[] file) {
        var notenPojo = new Noten(null, kompositionId,
                                  noten.instrument().name(),
                                  ofNullable(noten.stimme()).map(Enum::name).orElse(null),
                                  ofNullable(noten.stimmlage()).map(Enum::name).orElse(null));
        notenDao.insert(notenPojo);
        storageService.write(notenPojo.getId(), file);
    }

    public List<NotenDto> findByKomposition(Long kompositionId) {
        return notenDao.fetchByFkKomposition(kompositionId).stream()
                       .map(noten -> new NotenDto(noten.getId(),
                                                  Instrument.valueOf(noten.getInstrument()),
                                                  Stimme.of(noten.getStimme()).orElse(null),
                                                  Stimmlage.of(noten.getStimmlage()).orElse(null)))
                       .sorted(Comparator.comparing(NotenDto::instrument)
                                         .thenComparing(NotenDto::stimme, nullsLast(naturalOrder()))
                                         .thenComparing(NotenDto::stimmlage, nullsLast(naturalOrder())))
                       .toList();
    }

}
