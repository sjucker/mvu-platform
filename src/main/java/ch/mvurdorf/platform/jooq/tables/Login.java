/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables;


import ch.mvurdorf.platform.jooq.Keys;
import ch.mvurdorf.platform.jooq.Public;
import ch.mvurdorf.platform.jooq.tables.AbsenzStatus.AbsenzStatusPath;
import ch.mvurdorf.platform.jooq.tables.Event.EventPath;
import ch.mvurdorf.platform.jooq.tables.records.LoginRecord;

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
public class Login extends TableImpl<LoginRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.login</code>
     */
    public static final Login LOGIN = new Login();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<LoginRecord> getRecordType() {
        return LoginRecord.class;
    }

    /**
     * The column <code>public.login.id</code>.
     */
    public final TableField<LoginRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.login.email</code>.
     */
    public final TableField<LoginRecord, String> EMAIL = createField(DSL.name("email"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.login.name</code>.
     */
    public final TableField<LoginRecord, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.login.active</code>.
     */
    public final TableField<LoginRecord, Boolean> ACTIVE = createField(DSL.name("active"), SQLDataType.BOOLEAN.nullable(false), this, "");

    /**
     * The column <code>public.login.last_login</code>.
     */
    public final TableField<LoginRecord, LocalDateTime> LAST_LOGIN = createField(DSL.name("last_login"), SQLDataType.LOCALDATETIME(6), this, "");

    /**
     * The column <code>public.login.users_permission</code>.
     */
    public final TableField<LoginRecord, String> USERS_PERMISSION = createField(DSL.name("users_permission"), SQLDataType.VARCHAR(255).nullable(false).defaultValue(DSL.field(DSL.raw("'NONE'::character varying"), SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.login.noten_permission</code>.
     */
    public final TableField<LoginRecord, String> NOTEN_PERMISSION = createField(DSL.name("noten_permission"), SQLDataType.VARCHAR(255).nullable(false).defaultValue(DSL.field(DSL.raw("'NONE'::character varying"), SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.login.supporter_permission</code>.
     */
    public final TableField<LoginRecord, String> SUPPORTER_PERMISSION = createField(DSL.name("supporter_permission"), SQLDataType.VARCHAR(255).nullable(false).defaultValue(DSL.field(DSL.raw("'NONE'::character varying"), SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.login.konzerte_permission</code>.
     */
    public final TableField<LoginRecord, String> KONZERTE_PERMISSION = createField(DSL.name("konzerte_permission"), SQLDataType.VARCHAR(255).nullable(false).defaultValue(DSL.field(DSL.raw("'NONE'::character varying"), SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.login.repertoire_permission</code>.
     */
    public final TableField<LoginRecord, String> REPERTOIRE_PERMISSION = createField(DSL.name("repertoire_permission"), SQLDataType.VARCHAR(255).nullable(false).defaultValue(DSL.field(DSL.raw("'NONE'::character varying"), SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.login.event_permission</code>.
     */
    public final TableField<LoginRecord, String> EVENT_PERMISSION = createField(DSL.name("event_permission"), SQLDataType.VARCHAR(255).nullable(false).defaultValue(DSL.field(DSL.raw("'NONE'::character varying"), SQLDataType.VARCHAR)), this, "");

    private Login(Name alias, Table<LoginRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Login(Name alias, Table<LoginRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.login</code> table reference
     */
    public Login(String alias) {
        this(DSL.name(alias), LOGIN);
    }

    /**
     * Create an aliased <code>public.login</code> table reference
     */
    public Login(Name alias) {
        this(alias, LOGIN);
    }

    /**
     * Create a <code>public.login</code> table reference
     */
    public Login() {
        this(DSL.name("login"), null);
    }

    public <O extends Record> Login(Table<O> path, ForeignKey<O, LoginRecord> childPath, InverseForeignKey<O, LoginRecord> parentPath) {
        super(path, childPath, parentPath, LOGIN);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class LoginPath extends Login implements Path<LoginRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> LoginPath(Table<O> path, ForeignKey<O, LoginRecord> childPath, InverseForeignKey<O, LoginRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private LoginPath(Name alias, Table<LoginRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public LoginPath as(String alias) {
            return new LoginPath(DSL.name(alias), this);
        }

        @Override
        public LoginPath as(Name alias) {
            return new LoginPath(alias, this);
        }

        @Override
        public LoginPath as(Table<?> alias) {
            return new LoginPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<LoginRecord, Long> getIdentity() {
        return (Identity<LoginRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<LoginRecord> getPrimaryKey() {
        return Keys.PK__LOGIN;
    }

    @Override
    public List<UniqueKey<LoginRecord>> getUniqueKeys() {
        return Arrays.asList(Keys.UQ__LOGIN_EMAIL);
    }

    private transient AbsenzStatusPath _absenzStatus;

    /**
     * Get the implicit to-many join path to the
     * <code>public.absenz_status</code> table
     */
    public AbsenzStatusPath absenzStatus() {
        if (_absenzStatus == null)
            _absenzStatus = new AbsenzStatusPath(this, null, Keys.ABSENZ_STATUS__FK__ABSENZ_STATUS_LOGIN.getInverseKey());

        return _absenzStatus;
    }

    /**
     * Get the implicit many-to-many join path to the <code>public.event</code>
     * table
     */
    public EventPath event() {
        return absenzStatus().event();
    }

    @Override
    public Login as(String alias) {
        return new Login(DSL.name(alias), this);
    }

    @Override
    public Login as(Name alias) {
        return new Login(alias, this);
    }

    @Override
    public Login as(Table<?> alias) {
        return new Login(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Login rename(String name) {
        return new Login(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Login rename(Name name) {
        return new Login(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Login rename(Table<?> name) {
        return new Login(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Login where(Condition condition) {
        return new Login(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Login where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Login where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Login where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Login where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Login where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Login where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Login where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Login whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Login whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
