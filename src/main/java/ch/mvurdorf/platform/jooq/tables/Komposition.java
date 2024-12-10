/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables;


import ch.mvurdorf.platform.jooq.Keys;
import ch.mvurdorf.platform.jooq.Public;
import ch.mvurdorf.platform.jooq.tables.Noten.NotenPath;
import ch.mvurdorf.platform.jooq.tables.records.KompositionRecord;

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
public class Komposition extends TableImpl<KompositionRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.komposition</code>
     */
    public static final Komposition KOMPOSITION = new Komposition();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<KompositionRecord> getRecordType() {
        return KompositionRecord.class;
    }

    /**
     * The column <code>public.komposition.id</code>.
     */
    public final TableField<KompositionRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.komposition.titel</code>.
     */
    public final TableField<KompositionRecord, String> TITEL = createField(DSL.name("titel"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.komposition.komponist</code>.
     */
    public final TableField<KompositionRecord, String> KOMPONIST = createField(DSL.name("komponist"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.komposition.arrangeur</code>.
     */
    public final TableField<KompositionRecord, String> ARRANGEUR = createField(DSL.name("arrangeur"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.komposition.durationinseconds</code>.
     */
    public final TableField<KompositionRecord, Integer> DURATIONINSECONDS = createField(DSL.name("durationinseconds"), SQLDataType.INTEGER, this, "");

    private Komposition(Name alias, Table<KompositionRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Komposition(Name alias, Table<KompositionRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.komposition</code> table reference
     */
    public Komposition(String alias) {
        this(DSL.name(alias), KOMPOSITION);
    }

    /**
     * Create an aliased <code>public.komposition</code> table reference
     */
    public Komposition(Name alias) {
        this(alias, KOMPOSITION);
    }

    /**
     * Create a <code>public.komposition</code> table reference
     */
    public Komposition() {
        this(DSL.name("komposition"), null);
    }

    public <O extends Record> Komposition(Table<O> path, ForeignKey<O, KompositionRecord> childPath, InverseForeignKey<O, KompositionRecord> parentPath) {
        super(path, childPath, parentPath, KOMPOSITION);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class KompositionPath extends Komposition implements Path<KompositionRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> KompositionPath(Table<O> path, ForeignKey<O, KompositionRecord> childPath, InverseForeignKey<O, KompositionRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private KompositionPath(Name alias, Table<KompositionRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public KompositionPath as(String alias) {
            return new KompositionPath(DSL.name(alias), this);
        }

        @Override
        public KompositionPath as(Name alias) {
            return new KompositionPath(alias, this);
        }

        @Override
        public KompositionPath as(Table<?> alias) {
            return new KompositionPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<KompositionRecord, Long> getIdentity() {
        return (Identity<KompositionRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<KompositionRecord> getPrimaryKey() {
        return Keys.PK__KOMPOSITION;
    }

    private transient NotenPath _noten;

    /**
     * Get the implicit to-many join path to the <code>public.noten</code> table
     */
    public NotenPath noten() {
        if (_noten == null)
            _noten = new NotenPath(this, null, Keys.NOTEN__NOTEN_FK_KOMPOSITION_FKEY.getInverseKey());

        return _noten;
    }

    @Override
    public Komposition as(String alias) {
        return new Komposition(DSL.name(alias), this);
    }

    @Override
    public Komposition as(Name alias) {
        return new Komposition(alias, this);
    }

    @Override
    public Komposition as(Table<?> alias) {
        return new Komposition(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Komposition rename(String name) {
        return new Komposition(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Komposition rename(Name name) {
        return new Komposition(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Komposition rename(Table<?> name) {
        return new Komposition(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Komposition where(Condition condition) {
        return new Komposition(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Komposition where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Komposition where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Komposition where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Komposition where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Komposition where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Komposition where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Komposition where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Komposition whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Komposition whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
