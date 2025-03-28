/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables.records;


import ch.mvurdorf.platform.jooq.tables.Supporter;

import java.time.LocalDateTime;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class SupporterRecord extends UpdatableRecordImpl<SupporterRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.supporter.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.supporter.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.supporter.vorname</code>.
     */
    public void setVorname(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.supporter.vorname</code>.
     */
    public String getVorname() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.supporter.nachname</code>.
     */
    public void setNachname(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.supporter.nachname</code>.
     */
    public String getNachname() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.supporter.strasse</code>.
     */
    public void setStrasse(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.supporter.strasse</code>.
     */
    public String getStrasse() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.supporter.ort</code>.
     */
    public void setOrt(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.supporter.ort</code>.
     */
    public String getOrt() {
        return (String) get(4);
    }

    /**
     * Setter for <code>public.supporter.email</code>.
     */
    public void setEmail(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.supporter.email</code>.
     */
    public String getEmail() {
        return (String) get(5);
    }

    /**
     * Setter for <code>public.supporter.bemerkung</code>.
     */
    public void setBemerkung(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.supporter.bemerkung</code>.
     */
    public String getBemerkung() {
        return (String) get(6);
    }

    /**
     * Setter for <code>public.supporter.kommunikation_post</code>.
     */
    public void setKommunikationPost(Boolean value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.supporter.kommunikation_post</code>.
     */
    public Boolean getKommunikationPost() {
        return (Boolean) get(7);
    }

    /**
     * Setter for <code>public.supporter.kommunikation_email</code>.
     */
    public void setKommunikationEmail(Boolean value) {
        set(8, value);
    }

    /**
     * Getter for <code>public.supporter.kommunikation_email</code>.
     */
    public Boolean getKommunikationEmail() {
        return (Boolean) get(8);
    }

    /**
     * Setter for <code>public.supporter.registered_at</code>.
     */
    public void setRegisteredAt(LocalDateTime value) {
        set(9, value);
    }

    /**
     * Getter for <code>public.supporter.registered_at</code>.
     */
    public LocalDateTime getRegisteredAt() {
        return (LocalDateTime) get(9);
    }

    /**
     * Setter for <code>public.supporter.external_id</code>.
     */
    public void setExternalId(Long value) {
        set(10, value);
    }

    /**
     * Getter for <code>public.supporter.external_id</code>.
     */
    public Long getExternalId() {
        return (Long) get(10);
    }

    /**
     * Setter for <code>public.supporter.anrede</code>.
     */
    public void setAnrede(String value) {
        set(11, value);
    }

    /**
     * Getter for <code>public.supporter.anrede</code>.
     */
    public String getAnrede() {
        return (String) get(11);
    }

    /**
     * Setter for <code>public.supporter.uuid</code>.
     */
    public void setUuid(String value) {
        set(12, value);
    }

    /**
     * Getter for <code>public.supporter.uuid</code>.
     */
    public String getUuid() {
        return (String) get(12);
    }

    /**
     * Setter for <code>public.supporter.plz</code>.
     */
    public void setPlz(String value) {
        set(13, value);
    }

    /**
     * Getter for <code>public.supporter.plz</code>.
     */
    public String getPlz() {
        return (String) get(13);
    }

    /**
     * Setter for <code>public.supporter.strasse_nr</code>.
     */
    public void setStrasseNr(String value) {
        set(14, value);
    }

    /**
     * Getter for <code>public.supporter.strasse_nr</code>.
     */
    public String getStrasseNr() {
        return (String) get(14);
    }

    /**
     * Setter for <code>public.supporter.country_code</code>.
     */
    public void setCountryCode(String value) {
        set(15, value);
    }

    /**
     * Getter for <code>public.supporter.country_code</code>.
     */
    public String getCountryCode() {
        return (String) get(15);
    }

    /**
     * Setter for <code>public.supporter.type</code>.
     */
    public void setType(String value) {
        set(16, value);
    }

    /**
     * Getter for <code>public.supporter.type</code>.
     */
    public String getType() {
        return (String) get(16);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached SupporterRecord
     */
    public SupporterRecord() {
        super(Supporter.SUPPORTER);
    }

    /**
     * Create a detached, initialised SupporterRecord
     */
    public SupporterRecord(Long id, String vorname, String nachname, String strasse, String ort, String email, String bemerkung, Boolean kommunikationPost, Boolean kommunikationEmail, LocalDateTime registeredAt, Long externalId, String anrede, String uuid, String plz, String strasseNr, String countryCode, String type) {
        super(Supporter.SUPPORTER);

        setId(id);
        setVorname(vorname);
        setNachname(nachname);
        setStrasse(strasse);
        setOrt(ort);
        setEmail(email);
        setBemerkung(bemerkung);
        setKommunikationPost(kommunikationPost);
        setKommunikationEmail(kommunikationEmail);
        setRegisteredAt(registeredAt);
        setExternalId(externalId);
        setAnrede(anrede);
        setUuid(uuid);
        setPlz(plz);
        setStrasseNr(strasseNr);
        setCountryCode(countryCode);
        setType(type);
        resetChangedOnNotNull();
    }

    /**
     * Create a detached, initialised SupporterRecord
     */
    public SupporterRecord(ch.mvurdorf.platform.jooq.tables.pojos.Supporter value) {
        super(Supporter.SUPPORTER);

        if (value != null) {
            setId(value.getId());
            setVorname(value.getVorname());
            setNachname(value.getNachname());
            setStrasse(value.getStrasse());
            setOrt(value.getOrt());
            setEmail(value.getEmail());
            setBemerkung(value.getBemerkung());
            setKommunikationPost(value.getKommunikationPost());
            setKommunikationEmail(value.getKommunikationEmail());
            setRegisteredAt(value.getRegisteredAt());
            setExternalId(value.getExternalId());
            setAnrede(value.getAnrede());
            setUuid(value.getUuid());
            setPlz(value.getPlz());
            setStrasseNr(value.getStrasseNr());
            setCountryCode(value.getCountryCode());
            setType(value.getType());
            resetChangedOnNotNull();
        }
    }
}
