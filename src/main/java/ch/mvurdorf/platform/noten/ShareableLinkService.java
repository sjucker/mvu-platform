package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.jooq.tables.daos.ShareableLinkDao;
import ch.mvurdorf.platform.jooq.tables.daos.ShareableLinkInstrumentDao;
import ch.mvurdorf.platform.jooq.tables.daos.ShareableLinkKompositionDao;
import ch.mvurdorf.platform.jooq.tables.pojos.ShareableLink;
import ch.mvurdorf.platform.jooq.tables.pojos.ShareableLinkInstrument;
import ch.mvurdorf.platform.jooq.tables.pojos.ShareableLinkKomposition;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static ch.mvurdorf.platform.jooq.Tables.KOMPOSITION;
import static ch.mvurdorf.platform.jooq.Tables.SHAREABLE_LINK;
import static ch.mvurdorf.platform.jooq.Tables.SHAREABLE_LINK_INSTRUMENT;
import static ch.mvurdorf.platform.jooq.Tables.SHAREABLE_LINK_KOMPOSITION;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

@Service
@RequiredArgsConstructor
public class ShareableLinkService {

    private final DSLContext jooqDsl;
    private final ShareableLinkDao shareableLinkDao;
    private final ShareableLinkKompositionDao shareableLinkKompositionDao;
    private final ShareableLinkInstrumentDao shareableLinkInstrumentDao;

    @Transactional
    public String createShareableLink(ShareableLinkDto dto) {
        var uuid = UUID.randomUUID().toString();
        var shareableLink = new ShareableLink(null, uuid, dto.description(), dto.validUntil());
        shareableLinkDao.insert(shareableLink);

        dto.kompositions().forEach(komposition -> shareableLinkKompositionDao.insert(new ShareableLinkKomposition(null,
                                                                                                                  shareableLink.getId(),
                                                                                                                  komposition.id())));
        dto.instruments().forEach(instrument -> shareableLinkInstrumentDao.insert(new ShareableLinkInstrument(null,
                                                                                                              shareableLink.getId(),
                                                                                                              instrument.name())));
        return uuid;
    }

    public Optional<ShareableLinkDto> findByUuid(String uuid) {
        return jooqDsl.select(SHAREABLE_LINK.ID,
                              SHAREABLE_LINK.DESCRIPTION,
                              SHAREABLE_LINK.VALID_UNTIL,
                              multiset(
                                      select(KOMPOSITION.ID,
                                             KOMPOSITION.TITEL,
                                             KOMPOSITION.AUDIO_SAMPLE)
                                              .from(SHAREABLE_LINK_KOMPOSITION)
                                              .join(KOMPOSITION).on(KOMPOSITION.ID.eq(SHAREABLE_LINK_KOMPOSITION.KOMPOSITION_ID))
                                              .where(SHAREABLE_LINK_KOMPOSITION.SHAREABLE_LINK_ID.eq(SHAREABLE_LINK.ID))
                              ).convertFrom(it -> it.map(r -> new KompositionDto(r.get(KOMPOSITION.ID), r.get(KOMPOSITION.TITEL), null, null, null, r.get(KOMPOSITION.AUDIO_SAMPLE), null, 0))),
                              multiset(
                                      select(SHAREABLE_LINK_INSTRUMENT.INSTRUMENT)
                                              .from(SHAREABLE_LINK_INSTRUMENT)
                                              .where(SHAREABLE_LINK_INSTRUMENT.SHAREABLE_LINK_ID.eq(SHAREABLE_LINK.ID))
                              ).convertFrom(it -> it.map(r -> Instrument.valueOf(r.get(SHAREABLE_LINK_INSTRUMENT.INSTRUMENT))
                              )))
                      .from(SHAREABLE_LINK)
                      .where(SHAREABLE_LINK.UUID.eq(uuid))
                      .fetchOptional(it -> new ShareableLinkDto(
                              it.get(SHAREABLE_LINK.DESCRIPTION),
                              it.get(SHAREABLE_LINK.VALID_UNTIL),
                              it.value4(),
                              new HashSet<>(it.value5())
                      ));
    }
}
