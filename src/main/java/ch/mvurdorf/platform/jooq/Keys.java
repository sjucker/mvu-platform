/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq;


import ch.mvurdorf.platform.jooq.tables.Komposition;
import ch.mvurdorf.platform.jooq.tables.Konzert;
import ch.mvurdorf.platform.jooq.tables.KonzertEntry;
import ch.mvurdorf.platform.jooq.tables.Login;
import ch.mvurdorf.platform.jooq.tables.Noten;
import ch.mvurdorf.platform.jooq.tables.Passivmitglied;
import ch.mvurdorf.platform.jooq.tables.PassivmitgliedPayment;
import ch.mvurdorf.platform.jooq.tables.PassivmitgliedVoucher;
import ch.mvurdorf.platform.jooq.tables.records.KompositionRecord;
import ch.mvurdorf.platform.jooq.tables.records.KonzertEntryRecord;
import ch.mvurdorf.platform.jooq.tables.records.KonzertRecord;
import ch.mvurdorf.platform.jooq.tables.records.LoginRecord;
import ch.mvurdorf.platform.jooq.tables.records.NotenRecord;
import ch.mvurdorf.platform.jooq.tables.records.PassivmitgliedPaymentRecord;
import ch.mvurdorf.platform.jooq.tables.records.PassivmitgliedRecord;
import ch.mvurdorf.platform.jooq.tables.records.PassivmitgliedVoucherRecord;

import org.jooq.ForeignKey;
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

    public static final UniqueKey<KompositionRecord> PK__KOMPOSITION = Internal.createUniqueKey(Komposition.KOMPOSITION, DSL.name("pk__komposition"), new TableField[] { Komposition.KOMPOSITION.ID }, true);
    public static final UniqueKey<KonzertRecord> PK__KONZERT = Internal.createUniqueKey(Konzert.KONZERT, DSL.name("pk__konzert"), new TableField[] { Konzert.KONZERT.ID }, true);
    public static final UniqueKey<KonzertEntryRecord> PK__KONZERT_ENTRY = Internal.createUniqueKey(KonzertEntry.KONZERT_ENTRY, DSL.name("pk__konzert_entry"), new TableField[] { KonzertEntry.KONZERT_ENTRY.ID }, true);
    public static final UniqueKey<LoginRecord> PK__LOGIN = Internal.createUniqueKey(Login.LOGIN, DSL.name("pk__login"), new TableField[] { Login.LOGIN.ID }, true);
    public static final UniqueKey<LoginRecord> UQ__LOGIN_EMAIL = Internal.createUniqueKey(Login.LOGIN, DSL.name("uq__login_email"), new TableField[] { Login.LOGIN.EMAIL }, true);
    public static final UniqueKey<NotenRecord> PK__NOTEN = Internal.createUniqueKey(Noten.NOTEN, DSL.name("pk__noten"), new TableField[] { Noten.NOTEN.ID }, true);
    public static final UniqueKey<PassivmitgliedRecord> PASSIVMITGLIED_EMAIL_KEY = Internal.createUniqueKey(Passivmitglied.PASSIVMITGLIED, DSL.name("passivmitglied_email_key"), new TableField[] { Passivmitglied.PASSIVMITGLIED.EMAIL }, true);
    public static final UniqueKey<PassivmitgliedRecord> PK__PASSIVMITGLIED = Internal.createUniqueKey(Passivmitglied.PASSIVMITGLIED, DSL.name("pk__passivmitglied"), new TableField[] { Passivmitglied.PASSIVMITGLIED.ID }, true);
    public static final UniqueKey<PassivmitgliedPaymentRecord> PK__PASSIVMITGLIED_PAYMENT = Internal.createUniqueKey(PassivmitgliedPayment.PASSIVMITGLIED_PAYMENT, DSL.name("pk__passivmitglied_payment"), new TableField[] { PassivmitgliedPayment.PASSIVMITGLIED_PAYMENT.ID }, true);
    public static final UniqueKey<PassivmitgliedVoucherRecord> PK__PASSIVMITGLIED_VOUCHER = Internal.createUniqueKey(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER, DSL.name("pk__passivmitglied_voucher"), new TableField[] { PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER.ID }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<KonzertEntryRecord, KompositionRecord> KONZERT_ENTRY__KONZERT_ENTRY_FK_KOMPOSITION_FKEY = Internal.createForeignKey(KonzertEntry.KONZERT_ENTRY, DSL.name("konzert_entry_fk_komposition_fkey"), new TableField[] { KonzertEntry.KONZERT_ENTRY.FK_KOMPOSITION }, Keys.PK__KOMPOSITION, new TableField[] { Komposition.KOMPOSITION.ID }, true);
    public static final ForeignKey<KonzertEntryRecord, KonzertRecord> KONZERT_ENTRY__KONZERT_ENTRY_FK_KONZERT_FKEY = Internal.createForeignKey(KonzertEntry.KONZERT_ENTRY, DSL.name("konzert_entry_fk_konzert_fkey"), new TableField[] { KonzertEntry.KONZERT_ENTRY.FK_KONZERT }, Keys.PK__KONZERT, new TableField[] { Konzert.KONZERT.ID }, true);
    public static final ForeignKey<NotenRecord, KompositionRecord> NOTEN__NOTEN_FK_KOMPOSITION_FKEY = Internal.createForeignKey(Noten.NOTEN, DSL.name("noten_fk_komposition_fkey"), new TableField[] { Noten.NOTEN.FK_KOMPOSITION }, Keys.PK__KOMPOSITION, new TableField[] { Komposition.KOMPOSITION.ID }, true);
    public static final ForeignKey<PassivmitgliedPaymentRecord, PassivmitgliedRecord> PASSIVMITGLIED_PAYMENT__PASSIVMITGLIED_PAYMENT_FK_PASSIVMITGLIED_FKEY = Internal.createForeignKey(PassivmitgliedPayment.PASSIVMITGLIED_PAYMENT, DSL.name("passivmitglied_payment_fk_passivmitglied_fkey"), new TableField[] { PassivmitgliedPayment.PASSIVMITGLIED_PAYMENT.FK_PASSIVMITGLIED }, Keys.PK__PASSIVMITGLIED, new TableField[] { Passivmitglied.PASSIVMITGLIED.ID }, true);
    public static final ForeignKey<PassivmitgliedVoucherRecord, PassivmitgliedRecord> PASSIVMITGLIED_VOUCHER__PASSIVMITGLIED_VOUCHER_FK_PASSIVMITGLIED_FKEY = Internal.createForeignKey(PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER, DSL.name("passivmitglied_voucher_fk_passivmitglied_fkey"), new TableField[] { PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER.FK_PASSIVMITGLIED }, Keys.PK__PASSIVMITGLIED, new TableField[] { Passivmitglied.PASSIVMITGLIED.ID }, true);
}
