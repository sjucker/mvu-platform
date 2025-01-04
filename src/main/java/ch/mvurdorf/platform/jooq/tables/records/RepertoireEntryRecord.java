/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables.records;


import ch.mvurdorf.platform.jooq.tables.RepertoireEntry;

import java.math.BigDecimal;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class RepertoireEntryRecord extends UpdatableRecordImpl<RepertoireEntryRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.repertoire_entry.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.repertoire_entry.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.repertoire_entry.fk_repertoire</code>.
     */
    public void setFkRepertoire(Long value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.repertoire_entry.fk_repertoire</code>.
     */
    public Long getFkRepertoire() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>public.repertoire_entry.fk_komposition</code>.
     */
    public void setFkKomposition(Long value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.repertoire_entry.fk_komposition</code>.
     */
    public Long getFkKomposition() {
        return (Long) get(2);
    }

    /**
     * Setter for <code>public.repertoire_entry.number</code>.
     */
    public void setNumber(BigDecimal value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.repertoire_entry.number</code>.
     */
    public BigDecimal getNumber() {
        return (BigDecimal) get(3);
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
     * Create a detached RepertoireEntryRecord
     */
    public RepertoireEntryRecord() {
        super(RepertoireEntry.REPERTOIRE_ENTRY);
    }

    /**
     * Create a detached, initialised RepertoireEntryRecord
     */
    public RepertoireEntryRecord(Long id, Long fkRepertoire, Long fkKomposition, BigDecimal number) {
        super(RepertoireEntry.REPERTOIRE_ENTRY);

        setId(id);
        setFkRepertoire(fkRepertoire);
        setFkKomposition(fkKomposition);
        setNumber(number);
        resetChangedOnNotNull();
    }

    /**
     * Create a detached, initialised RepertoireEntryRecord
     */
    public RepertoireEntryRecord(ch.mvurdorf.platform.jooq.tables.pojos.RepertoireEntry value) {
        super(RepertoireEntry.REPERTOIRE_ENTRY);

        if (value != null) {
            setId(value.getId());
            setFkRepertoire(value.getFkRepertoire());
            setFkKomposition(value.getFkKomposition());
            setNumber(value.getNumber());
            resetChangedOnNotNull();
        }
    }
}
