package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static ch.mvurdorf.platform.jooq.Tables.KOMPOSITION;
import static ch.mvurdorf.platform.jooq.Tables.KONZERT_ENTRY;
import static ch.mvurdorf.platform.jooq.Tables.NOTEN_PDF;
import static ch.mvurdorf.platform.jooq.Tables.NOTEN_PDF_ASSIGNMENT;
import static ch.mvurdorf.platform.jooq.Tables.NOTEN_SHARE_LINK;
import static org.jooq.impl.DSL.*;

@Service
@RequiredArgsConstructor
public class NotenShareService {

    private final DSLContext jooqDsl;
    private final StorageService storageService;
    private final NotenService notenService;

    public UUID createLink(Long konzertId, Instrument instrument, LocalDateTime expiresAt) {
        var token = UUID.randomUUID();
        jooqDsl.insertInto(NOTEN_SHARE_LINK)
               .columns(NOTEN_SHARE_LINK.FK_KONZERT,
                        NOTEN_SHARE_LINK.INSTRUMENT,
                        NOTEN_SHARE_LINK.TOKEN,
                        NOTEN_SHARE_LINK.EXPIRES_AT)
               .values(konzertId, instrument.name(), token, expiresAt)
               .execute();
        return token;
    }

    public List<Link> listActiveLinksForKonzert(Long konzertId) {
        var now = LocalDateTime.now();
        return jooqDsl.select(NOTEN_SHARE_LINK.ID,
                              NOTEN_SHARE_LINK.FK_KONZERT,
                              NOTEN_SHARE_LINK.INSTRUMENT,
                              NOTEN_SHARE_LINK.TOKEN,
                              NOTEN_SHARE_LINK.EXPIRES_AT,
                              NOTEN_SHARE_LINK.ACTIVE)
                      .from(NOTEN_SHARE_LINK)
                      .where(NOTEN_SHARE_LINK.FK_KONZERT.eq(konzertId)
                             .and(NOTEN_SHARE_LINK.ACTIVE.eq(true))
                             .and(NOTEN_SHARE_LINK.EXPIRES_AT.isNull().or(NOTEN_SHARE_LINK.EXPIRES_AT.gt(now))))
                      .orderBy(NOTEN_SHARE_LINK.INSTRUMENT.asc())
                      .fetch(r -> new Link(r.value1(), r.value2(), Instrument.valueOf(r.value3()), r.value4(), r.value5(), r.value6()));
    }

    public Optional<Link> findByToken(UUID token) {
        var now = LocalDateTime.now();
        return jooqDsl.select(NOTEN_SHARE_LINK.ID,
                              NOTEN_SHARE_LINK.FK_KONZERT,
                              NOTEN_SHARE_LINK.INSTRUMENT,
                              NOTEN_SHARE_LINK.TOKEN,
                              NOTEN_SHARE_LINK.EXPIRES_AT,
                              NOTEN_SHARE_LINK.ACTIVE)
                      .from(NOTEN_SHARE_LINK)
                      .where(NOTEN_SHARE_LINK.TOKEN.eq(token)
                             .and(NOTEN_SHARE_LINK.ACTIVE.eq(true))
                             .and(NOTEN_SHARE_LINK.EXPIRES_AT.isNull().or(NOTEN_SHARE_LINK.EXPIRES_AT.gt(now))))
                      .fetchOptional(r -> new Link(r.value1(), r.value2(), Instrument.valueOf(r.value3()), r.value4(), r.value5(), r.value6()));
    }

    public List<SharedPdf> listPdfsForToken(UUID token) {
        var link = findByToken(token).orElseThrow();

        return jooqDsl.select(NOTEN_PDF.ID,
                              KOMPOSITION.TITEL)
                      .from(NOTEN_PDF)
                      .join(KONZERT_ENTRY).on(KONZERT_ENTRY.FK_KOMPOSITION.eq(NOTEN_PDF.FK_KOMPOSITION))
                      .join(KOMPOSITION).on(KOMPOSITION.ID.eq(NOTEN_PDF.FK_KOMPOSITION))
                      .join(NOTEN_PDF_ASSIGNMENT).on(NOTEN_PDF_ASSIGNMENT.FK_NOTEN_PDF.eq(NOTEN_PDF.ID)
                                                    .and(NOTEN_PDF_ASSIGNMENT.INSTRUMENT.eq(link.instrument.name())))
                      .where(KONZERT_ENTRY.FK_KONZERT.eq(link.konzertId))
                      .orderBy(KOMPOSITION.TITEL.asc(), NOTEN_PDF.ID.asc())
                      .fetch(r -> new SharedPdf(r.value1(), r.value2()));
    }

    public byte[] mergedPdfForToken(UUID token) {
        var link = findByToken(token).orElseThrow();
        var kompositionIds = jooqDsl.selectDistinct(KONZERT_ENTRY.FK_KOMPOSITION)
                                    .from(KONZERT_ENTRY)
                                    .where(KONZERT_ENTRY.FK_KONZERT.eq(link.konzertId))
                                    .fetch(KONZERT_ENTRY.FK_KOMPOSITION);

        return notenService.exportNotenToPdf(kompositionIds,
                                             link.instrument,
                                             Set.of(), Set.of(), Set.of());
    }

    public Optional<byte[]> singlePdfForToken(UUID token, long notenId) {
        var allowed = listPdfsForToken(token).stream().anyMatch(p -> p.getId() == notenId);
        if (!allowed) return Optional.empty();
        return Optional.of(storageService.read(notenId));
    }

    @Value
    public static class Link {
        Long id;
        Long konzertId;
        Instrument instrument;
        UUID token;
        LocalDateTime expiresAt;
        Boolean active;
    }

    @Value
    public static class SharedPdf {
        long id;
        String titel;
    }
}
