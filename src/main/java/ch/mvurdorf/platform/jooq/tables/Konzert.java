/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables;


import ch.mvurdorf.platform.jooq.Keys;
import ch.mvurdorf.platform.jooq.Public;
import ch.mvurdorf.platform.jooq.tables.KonzertEntry.KonzertEntryPath;
import ch.mvurdorf.platform.jooq.tables.records.KonzertRecord;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;

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
public class Konzert extends TableImpl<KonzertRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.konzert</code>
     */
    public static final Konzert KONZERT = new Konzert();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<KonzertRecord> getRecordType() {
        return KonzertRecord.class;
    }

    /**
     * The column <code>public.konzert.id</code>.
     */
    public final TableField<KonzertRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.konzert.name</code>.
     */
    public final TableField<KonzertRecord, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.konzert.datum</code>.
     */
    public final TableField<KonzertRecord, LocalDate> DATUM = createField(DSL.name("datum"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.konzert.zeit</code>.
     */
    public final TableField<KonzertRecord, LocalTime> ZEIT = createField(DSL.name("zeit"), SQLDataType.LOCALTIME(6).nullable(false), this, "");

    /**
     * The column <code>public.konzert.location</code>.
     */
    public final TableField<KonzertRecord, String> LOCATION = createField(DSL.name("location"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.konzert.description</code>.
     */
    public final TableField<KonzertRecord, String> DESCRIPTION = createField(DSL.name("description"), SQLDataType.CLOB, this, "");

    private Konzert(Name alias, Table<KonzertRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Konzert(Name alias, Table<KonzertRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.konzert</code> table reference
     */
    public Konzert(String alias) {
        this(DSL.name(alias), KONZERT);
    }

    /**
     * Create an aliased <code>public.konzert</code> table reference
     */
    public Konzert(Name alias) {
        this(alias, KONZERT);
    }

    /**
     * Create a <code>public.konzert</code> table reference
     */
    public Konzert() {
        this(DSL.name("konzert"), null);
    }

    public <O extends Record> Konzert(Table<O> path, ForeignKey<O, KonzertRecord> childPath, InverseForeignKey<O, KonzertRecord> parentPath) {
        super(path, childPath, parentPath, KONZERT);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class KonzertPath extends Konzert implements Path<KonzertRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> KonzertPath(Table<O> path, ForeignKey<O, KonzertRecord> childPath, InverseForeignKey<O, KonzertRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private KonzertPath(Name alias, Table<KonzertRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public KonzertPath as(String alias) {
            return new KonzertPath(DSL.name(alias), this);
        }

        @Override
        public KonzertPath as(Name alias) {
            return new KonzertPath(alias, this);
        }

        @Override
        public KonzertPath as(Table<?> alias) {
            return new KonzertPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<KonzertRecord, Long> getIdentity() {
        return (Identity<KonzertRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<KonzertRecord> getPrimaryKey() {
        return Keys.PK__KONZERT;
    }

    private transient KonzertEntryPath _konzertEntry;

    /**
     * Get the implicit to-many join path to the
     * <code>public.konzert_entry</code> table
     */
    public KonzertEntryPath konzertEntry() {
        if (_konzertEntry == null)
            _konzertEntry = new KonzertEntryPath(this, null, Keys.KONZERT_ENTRY__KONZERT_ENTRY_FK_KONZERT_FKEY.getInverseKey());

        return _konzertEntry;
    }

    @Override
    public Konzert as(String alias) {
        return new Konzert(DSL.name(alias), this);
    }

    @Override
    public Konzert as(Name alias) {
        return new Konzert(alias, this);
    }

    @Override
    public Konzert as(Table<?> alias) {
        return new Konzert(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Konzert rename(String name) {
        return new Konzert(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Konzert rename(Name name) {
        return new Konzert(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Konzert rename(Table<?> name) {
        return new Konzert(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Konzert where(Condition condition) {
        return new Konzert(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Konzert where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Konzert where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Konzert where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Konzert where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Konzert where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Konzert where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Konzert where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Konzert whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Konzert whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
