/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables.records;


import ch.mvurdorf.platform.jooq.tables.Komposition;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class KompositionRecord extends UpdatableRecordImpl<KompositionRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.komposition.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.komposition.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.komposition.titel</code>.
     */
    public void setTitel(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.komposition.titel</code>.
     */
    public String getTitel() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.komposition.komponist</code>.
     */
    public void setKomponist(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.komposition.komponist</code>.
     */
    public String getKomponist() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.komposition.arrangeur</code>.
     */
    public void setArrangeur(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.komposition.arrangeur</code>.
     */
    public String getArrangeur() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.komposition.format</code>.
     */
    public void setFormat(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.komposition.format</code>.
     */
    public String getFormat() {
        return (String) get(4);
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
     * Create a detached KompositionRecord
     */
    public KompositionRecord() {
        super(Komposition.KOMPOSITION);
    }

    /**
     * Create a detached, initialised KompositionRecord
     */
    public KompositionRecord(Long id, String titel, String komponist, String arrangeur, String format) {
        super(Komposition.KOMPOSITION);

        setId(id);
        setTitel(titel);
        setKomponist(komponist);
        setArrangeur(arrangeur);
        setFormat(format);
        resetChangedOnNotNull();
    }

    /**
     * Create a detached, initialised KompositionRecord
     */
    public KompositionRecord(ch.mvurdorf.platform.jooq.tables.pojos.Komposition value) {
        super(Komposition.KOMPOSITION);

        if (value != null) {
            setId(value.getId());
            setTitel(value.getTitel());
            setKomponist(value.getKomponist());
            setArrangeur(value.getArrangeur());
            setFormat(value.getFormat());
            resetChangedOnNotNull();
        }
    }
}
