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


/**
 * Convenience access to all tables in public.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Tables {

    /**
     * The table <code>public.komposition</code>.
     */
    public static final Komposition KOMPOSITION = Komposition.KOMPOSITION;

    /**
     * The table <code>public.konzert</code>.
     */
    public static final Konzert KONZERT = Konzert.KONZERT;

    /**
     * The table <code>public.konzert_entry</code>.
     */
    public static final KonzertEntry KONZERT_ENTRY = KonzertEntry.KONZERT_ENTRY;

    /**
     * The table <code>public.login</code>.
     */
    public static final Login LOGIN = Login.LOGIN;

    /**
     * The table <code>public.noten</code>.
     */
    public static final Noten NOTEN = Noten.NOTEN;

    /**
     * The table <code>public.passivmitglied</code>.
     */
    public static final Passivmitglied PASSIVMITGLIED = Passivmitglied.PASSIVMITGLIED;

    /**
     * The table <code>public.passivmitglied_payment</code>.
     */
    public static final PassivmitgliedPayment PASSIVMITGLIED_PAYMENT = PassivmitgliedPayment.PASSIVMITGLIED_PAYMENT;

    /**
     * The table <code>public.passivmitglied_voucher</code>.
     */
    public static final PassivmitgliedVoucher PASSIVMITGLIED_VOUCHER = PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER;
}
