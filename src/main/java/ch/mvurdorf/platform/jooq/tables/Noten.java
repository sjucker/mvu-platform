/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables;


import ch.mvurdorf.platform.jooq.Keys;
import ch.mvurdorf.platform.jooq.Public;
import ch.mvurdorf.platform.jooq.tables.Komposition.KompositionPath;
import ch.mvurdorf.platform.jooq.tables.records.NotenRecord;

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
public class Noten extends TableImpl<NotenRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.noten</code>
     */
    public static final Noten NOTEN = new Noten();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<NotenRecord> getRecordType() {
        return NotenRecord.class;
    }

    /**
     * The column <code>public.noten.id</code>.
     */
    public final TableField<NotenRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.noten.fk_komposition</code>.
     */
    public final TableField<NotenRecord, Long> FK_KOMPOSITION = createField(DSL.name("fk_komposition"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.noten.instrument</code>.
     */
    public final TableField<NotenRecord, String> INSTRUMENT = createField(DSL.name("instrument"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.noten.stimme</code>.
     */
    public final TableField<NotenRecord, String> STIMME = createField(DSL.name("stimme"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.noten.stimmlage</code>.
     */
    public final TableField<NotenRecord, String> STIMMLAGE = createField(DSL.name("stimmlage"), SQLDataType.VARCHAR(255), this, "");

    private Noten(Name alias, Table<NotenRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Noten(Name alias, Table<NotenRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.noten</code> table reference
     */
    public Noten(String alias) {
        this(DSL.name(alias), NOTEN);
    }

    /**
     * Create an aliased <code>public.noten</code> table reference
     */
    public Noten(Name alias) {
        this(alias, NOTEN);
    }

    /**
     * Create a <code>public.noten</code> table reference
     */
    public Noten() {
        this(DSL.name("noten"), null);
    }

    public <O extends Record> Noten(Table<O> path, ForeignKey<O, NotenRecord> childPath, InverseForeignKey<O, NotenRecord> parentPath) {
        super(path, childPath, parentPath, NOTEN);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class NotenPath extends Noten implements Path<NotenRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> NotenPath(Table<O> path, ForeignKey<O, NotenRecord> childPath, InverseForeignKey<O, NotenRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private NotenPath(Name alias, Table<NotenRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public NotenPath as(String alias) {
            return new NotenPath(DSL.name(alias), this);
        }

        @Override
        public NotenPath as(Name alias) {
            return new NotenPath(alias, this);
        }

        @Override
        public NotenPath as(Table<?> alias) {
            return new NotenPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<NotenRecord, Long> getIdentity() {
        return (Identity<NotenRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<NotenRecord> getPrimaryKey() {
        return Keys.PK__NOTEN;
    }

    @Override
    public List<ForeignKey<NotenRecord, ?>> getReferences() {
        return Arrays.asList(Keys.NOTEN__NOTEN_FK_KOMPOSITION_FKEY);
    }

    private transient KompositionPath _komposition;

    /**
     * Get the implicit join path to the <code>public.komposition</code> table.
     */
    public KompositionPath komposition() {
        if (_komposition == null)
            _komposition = new KompositionPath(this, Keys.NOTEN__NOTEN_FK_KOMPOSITION_FKEY, null);

        return _komposition;
    }

    @Override
    public Noten as(String alias) {
        return new Noten(DSL.name(alias), this);
    }

    @Override
    public Noten as(Name alias) {
        return new Noten(alias, this);
    }

    @Override
    public Noten as(Table<?> alias) {
        return new Noten(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Noten rename(String name) {
        return new Noten(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Noten rename(Name name) {
        return new Noten(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Noten rename(Table<?> name) {
        return new Noten(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Noten where(Condition condition) {
        return new Noten(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Noten where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Noten where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Noten where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Noten where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Noten where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Noten where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Noten where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Noten whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Noten whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}