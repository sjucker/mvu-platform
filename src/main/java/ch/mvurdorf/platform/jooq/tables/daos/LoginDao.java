/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables.daos;

import ch.mvurdorf.platform.jooq.AbstractSpringDAOImpl;
import ch.mvurdorf.platform.jooq.tables.Login;
import ch.mvurdorf.platform.jooq.tables.records.LoginRecord;
import org.jooq.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes", "this-escape"})
@Repository
public class LoginDao extends AbstractSpringDAOImpl<LoginRecord, ch.mvurdorf.platform.jooq.tables.pojos.Login, String> {

    /**
     * Create a new LoginDao without any configuration
     */
    public LoginDao() {
        super(Login.LOGIN, ch.mvurdorf.platform.jooq.tables.pojos.Login.class);
    }

    /**
     * Create a new LoginDao with an attached configuration
     */
    @Autowired
    public LoginDao(Configuration configuration) {
        super(Login.LOGIN, ch.mvurdorf.platform.jooq.tables.pojos.Login.class, configuration);
    }

    @Override
    public String getId(ch.mvurdorf.platform.jooq.tables.pojos.Login object) {
        return object.getEmail();
    }

    /**
     * Fetch records that have <code>email BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.Login> fetchRangeOfEmail(String lowerInclusive, String upperInclusive) {
        return fetchRange(Login.LOGIN.EMAIL, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>email IN (values)</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.Login> fetchByEmail(String... values) {
        return fetch(Login.LOGIN.EMAIL, values);
    }

    /**
     * Fetch a unique record that has <code>email = value</code>
     */
    public ch.mvurdorf.platform.jooq.tables.pojos.Login fetchOneByEmail(String value) {
        return fetchOne(Login.LOGIN.EMAIL, value);
    }

    /**
     * Fetch a unique record that has <code>email = value</code>
     */
    public Optional<ch.mvurdorf.platform.jooq.tables.pojos.Login> fetchOptionalByEmail(String value) {
        return fetchOptional(Login.LOGIN.EMAIL, value);
    }

    /**
     * Fetch records that have <code>name BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.Login> fetchRangeOfName(String lowerInclusive, String upperInclusive) {
        return fetchRange(Login.LOGIN.NAME, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>name IN (values)</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.Login> fetchByName(String... values) {
        return fetch(Login.LOGIN.NAME, values);
    }

    /**
     * Fetch records that have <code>password BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.Login> fetchRangeOfPassword(String lowerInclusive, String upperInclusive) {
        return fetchRange(Login.LOGIN.PASSWORD, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>password IN (values)</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.Login> fetchByPassword(String... values) {
        return fetch(Login.LOGIN.PASSWORD, values);
    }

    /**
     * Fetch records that have <code>active BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.Login> fetchRangeOfActive(Boolean lowerInclusive, Boolean upperInclusive) {
        return fetchRange(Login.LOGIN.ACTIVE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>active IN (values)</code>
     */
    public List<ch.mvurdorf.platform.jooq.tables.pojos.Login> fetchByActive(Boolean... values) {
        return fetch(Login.LOGIN.ACTIVE, values);
    }
}
