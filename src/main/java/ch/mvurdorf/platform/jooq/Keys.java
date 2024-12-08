/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq;


import ch.mvurdorf.platform.jooq.tables.Login;
import ch.mvurdorf.platform.jooq.tables.records.LoginRecord;

import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in
 * public.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<LoginRecord> PK__LOGIN = Internal.createUniqueKey(Login.LOGIN, DSL.name("pk__login"), new TableField[] { Login.LOGIN.ID }, true);
    public static final UniqueKey<LoginRecord> UQ__LOGIN_EMAIL = Internal.createUniqueKey(Login.LOGIN, DSL.name("uq__login_email"), new TableField[] { Login.LOGIN.EMAIL }, true);
}
