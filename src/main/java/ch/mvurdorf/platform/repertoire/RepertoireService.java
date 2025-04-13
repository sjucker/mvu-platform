package ch.mvurdorf.platform.repertoire;

import ch.mvurdorf.platform.jooq.tables.daos.RepertoireDao;
import ch.mvurdorf.platform.jooq.tables.daos.RepertoireEntryDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Repertoire;
import ch.mvurdorf.platform.jooq.tables.pojos.RepertoireEntry;
import ch.mvurdorf.platform.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static ch.mvurdorf.platform.jooq.Tables.KOMPOSITION;
import static ch.mvurdorf.platform.jooq.Tables.REPERTOIRE;
import static ch.mvurdorf.platform.jooq.Tables.REPERTOIRE_ENTRY;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

@Slf4j
@RequiredArgsConstructor
@Service
public class RepertoireService {

    private final DSLContext jooqDsl;
    private final RepertoireDao repertoireDao;
    private final RepertoireEntryDao repertoireEntryDao;

    public Optional<RepertoireDto> findRepertoireByType(RepertoireType type) {
        return jooqDsl.select(
                              REPERTOIRE,
                              multiset(
                                      select(KOMPOSITION.ID,
                                             KOMPOSITION.TITEL,
                                             KOMPOSITION.KOMPONIST,
                                             KOMPOSITION.ARRANGEUR,
                                             KOMPOSITION.AUDIO_SAMPLE,
                                             REPERTOIRE_ENTRY.NUMBER)
                                              .from(REPERTOIRE_ENTRY)
                                              .leftJoin(KOMPOSITION).on(REPERTOIRE_ENTRY.FK_KOMPOSITION.eq(KOMPOSITION.ID))
                                              .where(REPERTOIRE_ENTRY.FK_REPERTOIRE.eq(REPERTOIRE.ID))
                                              .orderBy(REPERTOIRE_ENTRY.NUMBER.asc())
                              ).convertFrom(it -> it.map(mapping(RepertoireEntryDto::new))
                              ))
                      .from(REPERTOIRE)
                      .where(REPERTOIRE.TYPE.eq(type.name()))
                      .orderBy(REPERTOIRE.CREATED_AT.desc())
                      .limit(1)
                      .fetchOptional(it -> {
                          var repertoire = it.value1();
                          return new RepertoireDto(
                                  RepertoireType.valueOf(repertoire.getType()),
                                  repertoire.getCreatedAt(),
                                  repertoire.getCreatedBy(),
                                  repertoire.getDetails(),
                                  it.value2()
                          );
                      });

    }

    @Transactional
    public void save(List<RepertoireEntryDto> entries, RepertoireType repertoireType, String details, String user) {
        var repertoire = new Repertoire(null, repertoireType.name(), DateUtil.now(), details, user);
        repertoireDao.insert(repertoire);
        entries.forEach(entry -> repertoireEntryDao.insert(new RepertoireEntry(null, repertoire.getId(), entry.kompositionId(), entry.number())));
    }
}
