/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables;


import ch.mvurdorf.platform.jooq.Keys;
import ch.mvurdorf.platform.jooq.Public;
import ch.mvurdorf.platform.jooq.tables.Event.EventPath;
import ch.mvurdorf.platform.jooq.tables.Login.LoginPath;
import ch.mvurdorf.platform.jooq.tables.records.AbsenzStatusRecord;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
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
public class AbsenzStatus extends TableImpl<AbsenzStatusRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.absenz_status</code>
     */
    public static final AbsenzStatus ABSENZ_STATUS = new AbsenzStatus();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AbsenzStatusRecord> getRecordType() {
        return AbsenzStatusRecord.class;
    }

    /**
     * The column <code>public.absenz_status.fk_login</code>.
     */
    public final TableField<AbsenzStatusRecord, Long> FK_LOGIN = createField(DSL.name("fk_login"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.absenz_status.fk_event</code>.
     */
    public final TableField<AbsenzStatusRecord, Long> FK_EVENT = createField(DSL.name("fk_event"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.absenz_status.remark</code>.
     */
    public final TableField<AbsenzStatusRecord, String> REMARK = createField(DSL.name("remark"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.absenz_status.status</code>.
     */
    public final TableField<AbsenzStatusRecord, String> STATUS = createField(DSL.name("status"), SQLDataType.VARCHAR(50).nullable(false), this, "");

    private AbsenzStatus(Name alias, Table<AbsenzStatusRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private AbsenzStatus(Name alias, Table<AbsenzStatusRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.absenz_status</code> table reference
     */
    public AbsenzStatus(String alias) {
        this(DSL.name(alias), ABSENZ_STATUS);
    }

    /**
     * Create an aliased <code>public.absenz_status</code> table reference
     */
    public AbsenzStatus(Name alias) {
        this(alias, ABSENZ_STATUS);
    }

    /**
     * Create a <code>public.absenz_status</code> table reference
     */
    public AbsenzStatus() {
        this(DSL.name("absenz_status"), null);
    }

    public <O extends Record> AbsenzStatus(Table<O> path, ForeignKey<O, AbsenzStatusRecord> childPath, InverseForeignKey<O, AbsenzStatusRecord> parentPath) {
        super(path, childPath, parentPath, ABSENZ_STATUS);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class AbsenzStatusPath extends AbsenzStatus implements Path<AbsenzStatusRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> AbsenzStatusPath(Table<O> path, ForeignKey<O, AbsenzStatusRecord> childPath, InverseForeignKey<O, AbsenzStatusRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private AbsenzStatusPath(Name alias, Table<AbsenzStatusRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public AbsenzStatusPath as(String alias) {
            return new AbsenzStatusPath(DSL.name(alias), this);
        }

        @Override
        public AbsenzStatusPath as(Name alias) {
            return new AbsenzStatusPath(alias, this);
        }

        @Override
        public AbsenzStatusPath as(Table<?> alias) {
            return new AbsenzStatusPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<AbsenzStatusRecord> getPrimaryKey() {
        return Keys.PK__ABSENZ_STATUS;
    }

    @Override
    public List<ForeignKey<AbsenzStatusRecord, ?>> getReferences() {
        return Arrays.asList(Keys.ABSENZ_STATUS__FK__ABSENZ_STATUS_EVENT, Keys.ABSENZ_STATUS__FK__ABSENZ_STATUS_LOGIN);
    }

    private transient EventPath _event;

    /**
     * Get the implicit join path to the <code>public.event</code> table.
     */
    public EventPath event() {
        if (_event == null)
            _event = new EventPath(this, Keys.ABSENZ_STATUS__FK__ABSENZ_STATUS_EVENT, null);

        return _event;
    }

    private transient LoginPath _login;

    /**
     * Get the implicit join path to the <code>public.login</code> table.
     */
    public LoginPath login() {
        if (_login == null)
            _login = new LoginPath(this, Keys.ABSENZ_STATUS__FK__ABSENZ_STATUS_LOGIN, null);

        return _login;
    }

    @Override
    public AbsenzStatus as(String alias) {
        return new AbsenzStatus(DSL.name(alias), this);
    }

    @Override
    public AbsenzStatus as(Name alias) {
        return new AbsenzStatus(alias, this);
    }

    @Override
    public AbsenzStatus as(Table<?> alias) {
        return new AbsenzStatus(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public AbsenzStatus rename(String name) {
        return new AbsenzStatus(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public AbsenzStatus rename(Name name) {
        return new AbsenzStatus(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public AbsenzStatus rename(Table<?> name) {
        return new AbsenzStatus(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public AbsenzStatus where(Condition condition) {
        return new AbsenzStatus(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public AbsenzStatus where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public AbsenzStatus where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public AbsenzStatus where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public AbsenzStatus where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public AbsenzStatus where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public AbsenzStatus where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public AbsenzStatus where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public AbsenzStatus whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public AbsenzStatus whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
