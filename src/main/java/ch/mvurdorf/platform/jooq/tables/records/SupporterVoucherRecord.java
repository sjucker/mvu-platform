/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables.records;


import ch.mvurdorf.platform.jooq.tables.SupporterVoucher;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class SupporterVoucherRecord extends UpdatableRecordImpl<SupporterVoucherRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.supporter_voucher.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.supporter_voucher.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.supporter_voucher.fk_supporter</code>.
     */
    public void setFkSupporter(Long value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.supporter_voucher.fk_supporter</code>.
     */
    public Long getFkSupporter() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>public.supporter_voucher.code</code>.
     */
    public void setCode(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.supporter_voucher.code</code>.
     */
    public String getCode() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.supporter_voucher.description</code>.
     */
    public void setDescription(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.supporter_voucher.description</code>.
     */
    public String getDescription() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.supporter_voucher.valid_until</code>.
     */
    public void setValidUntil(LocalDate value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.supporter_voucher.valid_until</code>.
     */
    public LocalDate getValidUntil() {
        return (LocalDate) get(4);
    }

    /**
     * Setter for <code>public.supporter_voucher.redeemed_at</code>.
     */
    public void setRedeemedAt(LocalDateTime value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.supporter_voucher.redeemed_at</code>.
     */
    public LocalDateTime getRedeemedAt() {
        return (LocalDateTime) get(5);
    }

    /**
     * Setter for <code>public.supporter_voucher.redeemed_by</code>.
     */
    public void setRedeemedBy(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.supporter_voucher.redeemed_by</code>.
     */
    public String getRedeemedBy() {
        return (String) get(6);
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
     * Create a detached SupporterVoucherRecord
     */
    public SupporterVoucherRecord() {
        super(SupporterVoucher.SUPPORTER_VOUCHER);
    }

    /**
     * Create a detached, initialised SupporterVoucherRecord
     */
    public SupporterVoucherRecord(Long id, Long fkSupporter, String code, String description, LocalDate validUntil, LocalDateTime redeemedAt, String redeemedBy) {
        super(SupporterVoucher.SUPPORTER_VOUCHER);

        setId(id);
        setFkSupporter(fkSupporter);
        setCode(code);
        setDescription(description);
        setValidUntil(validUntil);
        setRedeemedAt(redeemedAt);
        setRedeemedBy(redeemedBy);
        resetChangedOnNotNull();
    }

    /**
     * Create a detached, initialised SupporterVoucherRecord
     */
    public SupporterVoucherRecord(ch.mvurdorf.platform.jooq.tables.pojos.SupporterVoucher value) {
        super(SupporterVoucher.SUPPORTER_VOUCHER);

        if (value != null) {
            setId(value.getId());
            setFkSupporter(value.getFkSupporter());
            setCode(value.getCode());
            setDescription(value.getDescription());
            setValidUntil(value.getValidUntil());
            setRedeemedAt(value.getRedeemedAt());
            setRedeemedBy(value.getRedeemedBy());
            resetChangedOnNotNull();
        }
    }
}