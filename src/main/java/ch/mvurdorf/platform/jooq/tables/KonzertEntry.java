/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables;


import ch.mvurdorf.platform.jooq.Keys;
import ch.mvurdorf.platform.jooq.Public;
import ch.mvurdorf.platform.jooq.tables.Komposition.KompositionPath;
import ch.mvurdorf.platform.jooq.tables.Konzert.KonzertPath;
import ch.mvurdorf.platform.jooq.tables.records.KonzertEntryRecord;

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
public class KonzertEntry extends TableImpl<KonzertEntryRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.konzert_entry</code>
     */
    public static final KonzertEntry KONZERT_ENTRY = new KonzertEntry();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<KonzertEntryRecord> getRecordType() {
        return KonzertEntryRecord.class;
    }

    /**
     * The column <code>public.konzert_entry.id</code>.
     */
    public final TableField<KonzertEntryRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.konzert_entry.fk_konzert</code>.
     */
    public final TableField<KonzertEntryRecord, Long> FK_KONZERT = createField(DSL.name("fk_konzert"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.konzert_entry.index</code>.
     */
    public final TableField<KonzertEntryRecord, Integer> INDEX = createField(DSL.name("index"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.konzert_entry.fk_komposition</code>.
     */
    public final TableField<KonzertEntryRecord, Long> FK_KOMPOSITION = createField(DSL.name("fk_komposition"), SQLDataType.BIGINT, this, "");

    /**
     * The column <code>public.konzert_entry.placeholder</code>.
     */
    public final TableField<KonzertEntryRecord, String> PLACEHOLDER = createField(DSL.name("placeholder"), SQLDataType.VARCHAR(255), this, "");

    private KonzertEntry(Name alias, Table<KonzertEntryRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private KonzertEntry(Name alias, Table<KonzertEntryRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.konzert_entry</code> table reference
     */
    public KonzertEntry(String alias) {
        this(DSL.name(alias), KONZERT_ENTRY);
    }

    /**
     * Create an aliased <code>public.konzert_entry</code> table reference
     */
    public KonzertEntry(Name alias) {
        this(alias, KONZERT_ENTRY);
    }

    /**
     * Create a <code>public.konzert_entry</code> table reference
     */
    public KonzertEntry() {
        this(DSL.name("konzert_entry"), null);
    }

    public <O extends Record> KonzertEntry(Table<O> path, ForeignKey<O, KonzertEntryRecord> childPath, InverseForeignKey<O, KonzertEntryRecord> parentPath) {
        super(path, childPath, parentPath, KONZERT_ENTRY);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class KonzertEntryPath extends KonzertEntry implements Path<KonzertEntryRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> KonzertEntryPath(Table<O> path, ForeignKey<O, KonzertEntryRecord> childPath, InverseForeignKey<O, KonzertEntryRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private KonzertEntryPath(Name alias, Table<KonzertEntryRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public KonzertEntryPath as(String alias) {
            return new KonzertEntryPath(DSL.name(alias), this);
        }

        @Override
        public KonzertEntryPath as(Name alias) {
            return new KonzertEntryPath(alias, this);
        }

        @Override
        public KonzertEntryPath as(Table<?> alias) {
            return new KonzertEntryPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<KonzertEntryRecord, Long> getIdentity() {
        return (Identity<KonzertEntryRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<KonzertEntryRecord> getPrimaryKey() {
        return Keys.PK__KONZERT_ENTRY;
    }

    @Override
    public List<ForeignKey<KonzertEntryRecord, ?>> getReferences() {
        return Arrays.asList(Keys.KONZERT_ENTRY__KONZERT_ENTRY_FK_KOMPOSITION_FKEY, Keys.KONZERT_ENTRY__KONZERT_ENTRY_FK_KONZERT_FKEY);
    }

    private transient KompositionPath _komposition;

    /**
     * Get the implicit join path to the <code>public.komposition</code> table.
     */
    public KompositionPath komposition() {
        if (_komposition == null)
            _komposition = new KompositionPath(this, Keys.KONZERT_ENTRY__KONZERT_ENTRY_FK_KOMPOSITION_FKEY, null);

        return _komposition;
    }

    private transient KonzertPath _konzert;

    /**
     * Get the implicit join path to the <code>public.konzert</code> table.
     */
    public KonzertPath konzert() {
        if (_konzert == null)
            _konzert = new KonzertPath(this, Keys.KONZERT_ENTRY__KONZERT_ENTRY_FK_KONZERT_FKEY, null);

        return _konzert;
    }

    @Override
    public KonzertEntry as(String alias) {
        return new KonzertEntry(DSL.name(alias), this);
    }

    @Override
    public KonzertEntry as(Name alias) {
        return new KonzertEntry(alias, this);
    }

    @Override
    public KonzertEntry as(Table<?> alias) {
        return new KonzertEntry(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public KonzertEntry rename(String name) {
        return new KonzertEntry(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public KonzertEntry rename(Name name) {
        return new KonzertEntry(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public KonzertEntry rename(Table<?> name) {
        return new KonzertEntry(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public KonzertEntry where(Condition condition) {
        return new KonzertEntry(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public KonzertEntry where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public KonzertEntry where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public KonzertEntry where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public KonzertEntry where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public KonzertEntry where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public KonzertEntry where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public KonzertEntry where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public KonzertEntry whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public KonzertEntry whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
