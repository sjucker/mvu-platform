/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables;


import ch.mvurdorf.platform.jooq.Keys;
import ch.mvurdorf.platform.jooq.Public;
import ch.mvurdorf.platform.jooq.tables.RepertoireEntry.RepertoireEntryPath;
import ch.mvurdorf.platform.jooq.tables.records.RepertoireRecord;

import java.time.LocalDateTime;
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
public class Repertoire extends TableImpl<RepertoireRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.repertoire</code>
     */
    public static final Repertoire REPERTOIRE = new Repertoire();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RepertoireRecord> getRecordType() {
        return RepertoireRecord.class;
    }

    /**
     * The column <code>public.repertoire.id</code>.
     */
    public final TableField<RepertoireRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.repertoire.type</code>.
     */
    public final TableField<RepertoireRecord, String> TYPE = createField(DSL.name("type"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.repertoire.created_at</code>.
     */
    public final TableField<RepertoireRecord, LocalDateTime> CREATED_AT = createField(DSL.name("created_at"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "");

    /**
     * The column <code>public.repertoire.details</code>.
     */
    public final TableField<RepertoireRecord, String> DETAILS = createField(DSL.name("details"), SQLDataType.CLOB, this, "");

    private Repertoire(Name alias, Table<RepertoireRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Repertoire(Name alias, Table<RepertoireRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.repertoire</code> table reference
     */
    public Repertoire(String alias) {
        this(DSL.name(alias), REPERTOIRE);
    }

    /**
     * Create an aliased <code>public.repertoire</code> table reference
     */
    public Repertoire(Name alias) {
        this(alias, REPERTOIRE);
    }

    /**
     * Create a <code>public.repertoire</code> table reference
     */
    public Repertoire() {
        this(DSL.name("repertoire"), null);
    }

    public <O extends Record> Repertoire(Table<O> path, ForeignKey<O, RepertoireRecord> childPath, InverseForeignKey<O, RepertoireRecord> parentPath) {
        super(path, childPath, parentPath, REPERTOIRE);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class RepertoirePath extends Repertoire implements Path<RepertoireRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> RepertoirePath(Table<O> path, ForeignKey<O, RepertoireRecord> childPath, InverseForeignKey<O, RepertoireRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private RepertoirePath(Name alias, Table<RepertoireRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public RepertoirePath as(String alias) {
            return new RepertoirePath(DSL.name(alias), this);
        }

        @Override
        public RepertoirePath as(Name alias) {
            return new RepertoirePath(alias, this);
        }

        @Override
        public RepertoirePath as(Table<?> alias) {
            return new RepertoirePath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<RepertoireRecord, Long> getIdentity() {
        return (Identity<RepertoireRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<RepertoireRecord> getPrimaryKey() {
        return Keys.PK__REPERTOIRE;
    }

    private transient RepertoireEntryPath _repertoireEntry;

    /**
     * Get the implicit to-many join path to the
     * <code>public.repertoire_entry</code> table
     */
    public RepertoireEntryPath repertoireEntry() {
        if (_repertoireEntry == null)
            _repertoireEntry = new RepertoireEntryPath(this, null, Keys.REPERTOIRE_ENTRY__REPERTOIRE_ENTRY_FK_REPERTOIRE_FKEY.getInverseKey());

        return _repertoireEntry;
    }

    @Override
    public Repertoire as(String alias) {
        return new Repertoire(DSL.name(alias), this);
    }

    @Override
    public Repertoire as(Name alias) {
        return new Repertoire(alias, this);
    }

    @Override
    public Repertoire as(Table<?> alias) {
        return new Repertoire(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Repertoire rename(String name) {
        return new Repertoire(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Repertoire rename(Name name) {
        return new Repertoire(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Repertoire rename(Table<?> name) {
        return new Repertoire(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Repertoire where(Condition condition) {
        return new Repertoire(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Repertoire where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Repertoire where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Repertoire where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Repertoire where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Repertoire where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Repertoire where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Repertoire where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Repertoire whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Repertoire whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}