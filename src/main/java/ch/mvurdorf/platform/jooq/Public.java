/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq;


import ch.mvurdorf.platform.jooq.tables.InstrumentPermission;
import ch.mvurdorf.platform.jooq.tables.Komposition;
import ch.mvurdorf.platform.jooq.tables.Konzert;
import ch.mvurdorf.platform.jooq.tables.KonzertEntry;
import ch.mvurdorf.platform.jooq.tables.Login;
import ch.mvurdorf.platform.jooq.tables.Noten;
import ch.mvurdorf.platform.jooq.tables.Repertoire;
import ch.mvurdorf.platform.jooq.tables.RepertoireEntry;
import ch.mvurdorf.platform.jooq.tables.Supporter;
import ch.mvurdorf.platform.jooq.tables.SupporterPayment;
import ch.mvurdorf.platform.jooq.tables.SupporterVoucher;
import ch.mvurdorf.platform.jooq.tables.Voucher;

import java.util.Arrays;
import java.util.List;

import org.jooq.Catalog;
import org.jooq.Sequence;
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
     * The table <code>public.instrument_permission</code>.
     */
    public final InstrumentPermission INSTRUMENT_PERMISSION = InstrumentPermission.INSTRUMENT_PERMISSION;

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
     * The table <code>public.repertoire</code>.
     */
    public final Repertoire REPERTOIRE = Repertoire.REPERTOIRE;

    /**
     * The table <code>public.repertoire_entry</code>.
     */
    public final RepertoireEntry REPERTOIRE_ENTRY = RepertoireEntry.REPERTOIRE_ENTRY;

    /**
     * The table <code>public.supporter</code>.
     */
    public final Supporter SUPPORTER = Supporter.SUPPORTER;

    /**
     * The table <code>public.supporter_payment</code>.
     */
    public final SupporterPayment SUPPORTER_PAYMENT = SupporterPayment.SUPPORTER_PAYMENT;

    /**
     * The table <code>public.supporter_voucher</code>.
     */
    public final SupporterVoucher SUPPORTER_VOUCHER = SupporterVoucher.SUPPORTER_VOUCHER;

    /**
     * The table <code>public.voucher</code>.
     */
    public final Voucher VOUCHER = Voucher.VOUCHER;

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
    public final List<Sequence<?>> getSequences() {
        return Arrays.asList(
            Sequences.PASSIVMITGLIED_ID_SEQ,
            Sequences.PASSIVMITGLIED_PAYMENT_ID_SEQ,
            Sequences.PASSIVMITGLIED_VOUCHER_ID_SEQ
        );
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            InstrumentPermission.INSTRUMENT_PERMISSION,
            Komposition.KOMPOSITION,
            Konzert.KONZERT,
            KonzertEntry.KONZERT_ENTRY,
            Login.LOGIN,
            Noten.NOTEN,
            Repertoire.REPERTOIRE,
            RepertoireEntry.REPERTOIRE_ENTRY,
            Supporter.SUPPORTER,
            SupporterPayment.SUPPORTER_PAYMENT,
            SupporterVoucher.SUPPORTER_VOUCHER,
            Voucher.VOUCHER
        );
    }
}
