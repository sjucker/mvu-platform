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

import java.util.Arrays;
import java.util.List;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.komposition</code>.
     */
    public final Komposition KOMPOSITION = Komposition.KOMPOSITION;

    /**
     * The table <code>public.konzert</code>.
     */
    public final Konzert KONZERT = Konzert.KONZERT;

    /**
     * The table <code>public.konzert_entry</code>.
     */
    public final KonzertEntry KONZERT_ENTRY = KonzertEntry.KONZERT_ENTRY;

    /**
     * The table <code>public.login</code>.
     */
    public final Login LOGIN = Login.LOGIN;

    /**
     * The table <code>public.noten</code>.
     */
    public final Noten NOTEN = Noten.NOTEN;

    /**
     * The table <code>public.passivmitglied</code>.
     */
    public final Passivmitglied PASSIVMITGLIED = Passivmitglied.PASSIVMITGLIED;

    /**
     * The table <code>public.passivmitglied_payment</code>.
     */
    public final PassivmitgliedPayment PASSIVMITGLIED_PAYMENT = PassivmitgliedPayment.PASSIVMITGLIED_PAYMENT;

    /**
     * The table <code>public.passivmitglied_voucher</code>.
     */
    public final PassivmitgliedVoucher PASSIVMITGLIED_VOUCHER = PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER;

    /**
     * No further instances allowed
     */
    private Public() {
        super("public", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            Komposition.KOMPOSITION,
            Konzert.KONZERT,
            KonzertEntry.KONZERT_ENTRY,
            Login.LOGIN,
            Noten.NOTEN,
            Passivmitglied.PASSIVMITGLIED,
            PassivmitgliedPayment.PASSIVMITGLIED_PAYMENT,
            PassivmitgliedVoucher.PASSIVMITGLIED_VOUCHER
        );
    }
}
