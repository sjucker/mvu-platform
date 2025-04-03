/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables.daos;


import ch.mvurdorf.platform.jooq.AbstractSpringDAOImpl;
import ch.mvurdorf.platform.jooq.tables.NotenPdf;
import ch.mvurdorf.platform.jooq.tables.records.NotenPdfRecord;

import java.util.List;
import java.util.Optional;

import org.jooq.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
@Repository
public class NotenPdfDao extends AbstractSpringDAOImpl<NotenPdfRecord, ch.mvurdorf.platform.jooq.tables.pojos.NotenPdf, Long> {

    /**
     * Create a new NotenPdfDao without any configuration
     */
    public NotenPdfDao() {
        super(NotenPdf.NOTEN_PDF, ch.mvurdorf.platform.jooq.tables.pojos.NotenPdf.class);
    }

    /**
     * Create a new NotenPdfDao with an attached configuration
     */
    @Autowired
    public NotenPdfDao(Configuration configuration) {
        super(NotenPdf.NOTEN_PDF, ch.mvurdorf.platform.jooq.tables.pojos.NotenPdf.class, configuration);
    }

    @Override
    public Long getId(ch.mvurdorf.platform.jooq.tables.pojos.NotenPdf object) {
        return object.getId();
    }

    /**
     * Fetch records that have <code>id BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.NotenPdf> fetchRangeOfId(Long lowerInclusive, Long upperInclusive) {
        return fetchRange(NotenPdf.NOTEN_PDF.ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>id IN (values)</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.NotenPdf> fetchById(Long... values) {
        return fetch(NotenPdf.NOTEN_PDF.ID, values);
    }

    /**
     * Fetch a unique record that has <code>id = value</code>
     */
    public ch.mvurdorf.platform.jooq.tables.pojos.NotenPdf fetchOneById(Long value) {
        return fetchOne(NotenPdf.NOTEN_PDF.ID, value);
    }

    /**
     * Fetch a unique record that has <code>id = value</code>
     */
    public Optional<ch.mvurdorf.platform.jooq.tables.pojos.NotenPdf> fetchOptionalById(Long value) {
        return fetchOptional(NotenPdf.NOTEN_PDF.ID, value);
    }

    /**
     * Fetch records that have <code>fk_komposition BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.NotenPdf> fetchRangeOfFkKomposition(Long lowerInclusive, Long upperInclusive) {
        return fetchRange(NotenPdf.NOTEN_PDF.FK_KOMPOSITION, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>fk_komposition IN (values)</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.NotenPdf> fetchByFkKomposition(Long... values) {
        return fetch(NotenPdf.NOTEN_PDF.FK_KOMPOSITION, values);
    }

    /**
     * Fetch records that have <code>stimmlage BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.NotenPdf> fetchRangeOfStimmlage(String lowerInclusive, String upperInclusive) {
        return fetchRange(NotenPdf.NOTEN_PDF.STIMMLAGE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>stimmlage IN (values)</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.NotenPdf> fetchByStimmlage(String... values) {
        return fetch(NotenPdf.NOTEN_PDF.STIMMLAGE, values);
    }
}
