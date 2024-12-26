/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables.daos;


import ch.mvurdorf.platform.jooq.AbstractSpringDAOImpl;
import ch.mvurdorf.platform.jooq.tables.PassivmitgliedVoucher;
import ch.mvurdorf.platform.jooq.tables.records.PassivmitgliedVoucherRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class PassivmitgliedVoucherDao extends AbstractSpringDAOImpl<PassivmitgliedVoucherRecord, ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher, Long> {

    /**
     * Create a new PassivmitgliedVoucherDao without any configuration
     */
    public PassivmitgliedVoucherDao() {
        super(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER, ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher.class);
    }

    /**
     * Create a new PassivmitgliedVoucherDao with an attached configuration
     */
    @Autowired
    public PassivmitgliedVoucherDao(Configuration configuration) {
        super(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER, ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher.class, configuration);
    }

    @Override
    public Long getId(ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher object) {
        return object.getId();
    }

    /**
     * Fetch records that have <code>id BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher> fetchRangeOfId(Long lowerInclusive, Long upperInclusive) {
        return fetchRange(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER.ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>id IN (values)</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher> fetchById(Long... values) {
        return fetch(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER.ID, values);
    }

    /**
     * Fetch a unique record that has <code>id = value</code>
     */
    public ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher fetchOneById(Long value) {
        return fetchOne(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER.ID, value);
    }

    /**
     * Fetch a unique record that has <code>id = value</code>
     */
    public Optional<ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher> fetchOptionalById(Long value) {
        return fetchOptional(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER.ID, value);
    }

    /**
     * Fetch records that have <code>fk_passivmitglied BETWEEN lowerInclusive
     * AND upperInclusive</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher> fetchRangeOfFkPassivmitglied(Long lowerInclusive, Long upperInclusive) {
        return fetchRange(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER.FK_PASSIVMITGLIED, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>fk_passivmitglied IN (values)</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher> fetchByFkPassivmitglied(Long... values) {
        return fetch(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER.FK_PASSIVMITGLIED, values);
    }

    /**
     * Fetch records that have <code>code BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher> fetchRangeOfCode(String lowerInclusive, String upperInclusive) {
        return fetchRange(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER.CODE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>code IN (values)</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher> fetchByCode(String... values) {
        return fetch(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER.CODE, values);
    }

    /**
     * Fetch records that have <code>description BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher> fetchRangeOfDescription(String lowerInclusive, String upperInclusive) {
        return fetchRange(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER.DESCRIPTION, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>description IN (values)</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher> fetchByDescription(String... values) {
        return fetch(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER.DESCRIPTION, values);
    }

    /**
     * Fetch records that have <code>valid_until BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher> fetchRangeOfValidUntil(LocalDate lowerInclusive, LocalDate upperInclusive) {
        return fetchRange(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER.VALID_UNTIL, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>valid_until IN (values)</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher> fetchByValidUntil(LocalDate... values) {
        return fetch(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER.VALID_UNTIL, values);
    }

    /**
     * Fetch records that have <code>redeemed_at BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher> fetchRangeOfRedeemedAt(LocalDateTime lowerInclusive, LocalDateTime upperInclusive) {
        return fetchRange(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER.REDEEMED_AT, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>redeemed_at IN (values)</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher> fetchByRedeemedAt(LocalDateTime... values) {
        return fetch(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER.REDEEMED_AT, values);
    }

    /**
     * Fetch records that have <code>redeemed_by BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher> fetchRangeOfRedeemedBy(String lowerInclusive, String upperInclusive) {
        return fetchRange(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER.REDEEMED_BY, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>redeemed_by IN (values)</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.PassivmitgliedVoucher> fetchByRedeemedBy(String... values) {
        return fetch(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER.REDEEMED_BY, values);
    }
}
