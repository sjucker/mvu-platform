/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables;


import ch.mvurdorf.platform.jooq.Keys;
import ch.mvurdorf.platform.jooq.Public;
import ch.mvurdorf.platform.jooq.tables.Supporter.SupporterPath;
import ch.mvurdorf.platform.jooq.tables.records.SupporterVoucherRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.InverseForeignKey;
import org.jooq.Name;
import org.jooq.Path;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.Record;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class SupporterVoucher extends TableImpl<SupporterVoucherRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.supporter_voucher</code>
     */
    public static final SupporterVoucher SUPPORTER_VOUCHER = new SupporterVoucher();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<SupporterVoucherRecord> getRecordType() {
        return SupporterVoucherRecord.class;
    }

    /**
     * The column <code>public.supporter_voucher.id</code>.
     */
    public final TableField<SupporterVoucherRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.supporter_voucher.fk_supporter</code>.
     */
    public final TableField<SupporterVoucherRecord, Long> FK_SUPPORTER = createField(DSL.name("fk_supporter"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.supporter_voucher.code</code>.
     */
    public final TableField<SupporterVoucherRecord, String> CODE = createField(DSL.name("code"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.supporter_voucher.description</code>.
     */
    public final TableField<SupporterVoucherRecord, String> DESCRIPTION = createField(DSL.name("description"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.supporter_voucher.valid_until</code>.
     */
    public final TableField<SupporterVoucherRecord, LocalDate> VALID_UNTIL = createField(DSL.name("valid_until"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.supporter_voucher.redeemed_at</code>.
     */
    public final TableField<SupporterVoucherRecord, LocalDateTime> REDEEMED_AT = createField(DSL.name("redeemed_at"), SQLDataType.LOCALDATETIME(6), this, "");

    /**
     * The column <code>public.supporter_voucher.redeemed_by</code>.
     */
    public final TableField<SupporterVoucherRecord, String> REDEEMED_BY = createField(DSL.name("redeemed_by"), SQLDataType.VARCHAR(255), this, "");

    private SupporterVoucher(Name alias, Table<SupporterVoucherRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private SupporterVoucher(Name alias, Table<SupporterVoucherRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.supporter_voucher</code> table reference
     */
    public SupporterVoucher(String alias) {
        this(DSL.name(alias), SUPPORTER_VOUCHER);
    }

    /**
     * Create an aliased <code>public.supporter_voucher</code> table reference
     */
    public SupporterVoucher(Name alias) {
        this(alias, SUPPORTER_VOUCHER);
    }

    /**
     * Create a <code>public.supporter_voucher</code> table reference
     */
    public SupporterVoucher() {
        this(DSL.name("supporter_voucher"), null);
    }

    public <O extends Record> SupporterVoucher(Table<O> path, ForeignKey<O, SupporterVoucherRecord> childPath, InverseForeignKey<O, SupporterVoucherRecord> parentPath) {
        super(path, childPath, parentPath, SUPPORTER_VOUCHER);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class SupporterVoucherPath extends SupporterVoucher implements Path<SupporterVoucherRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> SupporterVoucherPath(Table<O> path, ForeignKey<O, SupporterVoucherRecord> childPath, InverseForeignKey<O, SupporterVoucherRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private SupporterVoucherPath(Name alias, Table<SupporterVoucherRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public SupporterVoucherPath as(String alias) {
            return new SupporterVoucherPath(DSL.name(alias), this);
        }

        @Override
        public SupporterVoucherPath as(Name alias) {
            return new SupporterVoucherPath(alias, this);
        }

        @Override
        public SupporterVoucherPath as(Table<?> alias) {
            return new SupporterVoucherPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<SupporterVoucherRecord, Long> getIdentity() {
        return (Identity<SupporterVoucherRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<SupporterVoucherRecord> getPrimaryKey() {
        return Keys.PK__PASSIVMITGLIED_VOUCHER;
    }

    @Override
    public List<ForeignKey<SupporterVoucherRecord, ?>> getReferences() {
        return Arrays.asList(Keys.SUPPORTER_VOUCHER__PASSIVMITGLIED_VOUCHER_FK_PASSIVMITGLIED_FKEY);
    }

    private transient SupporterPath _supporter;

    /**
     * Get the implicit join path to the <code>public.supporter</code> table.
     */
    public SupporterPath supporter() {
        if (_supporter == null)
            _supporter = new SupporterPath(this, Keys.SUPPORTER_VOUCHER__PASSIVMITGLIED_VOUCHER_FK_PASSIVMITGLIED_FKEY, null);

        return _supporter;
    }

    @Override
    public SupporterVoucher as(String alias) {
        return new SupporterVoucher(DSL.name(alias), this);
    }

    @Override
    public SupporterVoucher as(Name alias) {
        return new SupporterVoucher(alias, this);
    }

    @Override
    public SupporterVoucher as(Table<?> alias) {
        return new SupporterVoucher(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public SupporterVoucher rename(String name) {
        return new SupporterVoucher(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public SupporterVoucher rename(Name name) {
        return new SupporterVoucher(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public SupporterVoucher rename(Table<?> name) {
        return new SupporterVoucher(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public SupporterVoucher where(Condition condition) {
        return new SupporterVoucher(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public SupporterVoucher where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public SupporterVoucher where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public SupporterVoucher where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public SupporterVoucher where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public SupporterVoucher where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public SupporterVoucher where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public SupporterVoucher where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public SupporterVoucher whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public SupporterVoucher whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}