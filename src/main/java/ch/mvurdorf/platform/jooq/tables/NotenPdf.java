/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables;


import ch.mvurdorf.platform.jooq.Keys;
import ch.mvurdorf.platform.jooq.Public;
import ch.mvurdorf.platform.jooq.tables.Komposition.KompositionPath;
import ch.mvurdorf.platform.jooq.tables.NotenPdfAssignment.NotenPdfAssignmentPath;
import ch.mvurdorf.platform.jooq.tables.records.NotenPdfRecord;

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
public class NotenPdf extends TableImpl<NotenPdfRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.noten_pdf</code>
     */
    public static final NotenPdf NOTEN_PDF = new NotenPdf();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<NotenPdfRecord> getRecordType() {
        return NotenPdfRecord.class;
    }

    /**
     * The column <code>public.noten_pdf.id</code>.
     */
    public final TableField<NotenPdfRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.noten_pdf.fk_komposition</code>.
     */
    public final TableField<NotenPdfRecord, Long> FK_KOMPOSITION = createField(DSL.name("fk_komposition"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.noten_pdf.stimmlage</code>.
     */
    public final TableField<NotenPdfRecord, String> STIMMLAGE = createField(DSL.name("stimmlage"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.noten_pdf.notenschluessel</code>.
     */
    public final TableField<NotenPdfRecord, String> NOTENSCHLUESSEL = createField(DSL.name("notenschluessel"), SQLDataType.VARCHAR(255), this, "");

    private NotenPdf(Name alias, Table<NotenPdfRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private NotenPdf(Name alias, Table<NotenPdfRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.noten_pdf</code> table reference
     */
    public NotenPdf(String alias) {
        this(DSL.name(alias), NOTEN_PDF);
    }

    /**
     * Create an aliased <code>public.noten_pdf</code> table reference
     */
    public NotenPdf(Name alias) {
        this(alias, NOTEN_PDF);
    }

    /**
     * Create a <code>public.noten_pdf</code> table reference
     */
    public NotenPdf() {
        this(DSL.name("noten_pdf"), null);
    }

    public <O extends Record> NotenPdf(Table<O> path, ForeignKey<O, NotenPdfRecord> childPath, InverseForeignKey<O, NotenPdfRecord> parentPath) {
        super(path, childPath, parentPath, NOTEN_PDF);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class NotenPdfPath extends NotenPdf implements Path<NotenPdfRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> NotenPdfPath(Table<O> path, ForeignKey<O, NotenPdfRecord> childPath, InverseForeignKey<O, NotenPdfRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private NotenPdfPath(Name alias, Table<NotenPdfRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public NotenPdfPath as(String alias) {
            return new NotenPdfPath(DSL.name(alias), this);
        }

        @Override
        public NotenPdfPath as(Name alias) {
            return new NotenPdfPath(alias, this);
        }

        @Override
        public NotenPdfPath as(Table<?> alias) {
            return new NotenPdfPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<NotenPdfRecord, Long> getIdentity() {
        return (Identity<NotenPdfRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<NotenPdfRecord> getPrimaryKey() {
        return Keys.PK__NOTEN;
    }

    @Override
    public List<ForeignKey<NotenPdfRecord, ?>> getReferences() {
        return Arrays.asList(Keys.NOTEN_PDF__NOTEN_FK_KOMPOSITION_FKEY);
    }

    private transient KompositionPath _komposition;

    /**
     * Get the implicit join path to the <code>public.komposition</code> table.
     */
    public KompositionPath komposition() {
        if (_komposition == null)
            _komposition = new KompositionPath(this, Keys.NOTEN_PDF__NOTEN_FK_KOMPOSITION_FKEY, null);

        return _komposition;
    }

    private transient NotenPdfAssignmentPath _notenPdfAssignment;

    /**
     * Get the implicit to-many join path to the
     * <code>public.noten_pdf_assignment</code> table
     */
    public NotenPdfAssignmentPath notenPdfAssignment() {
        if (_notenPdfAssignment == null)
            _notenPdfAssignment = new NotenPdfAssignmentPath(this, null, Keys.NOTEN_PDF_ASSIGNMENT__FK__NOTEN_PDF_ASSIGNMENT.getInverseKey());

        return _notenPdfAssignment;
    }

    @Override
    public NotenPdf as(String alias) {
        return new NotenPdf(DSL.name(alias), this);
    }

    @Override
    public NotenPdf as(Name alias) {
        return new NotenPdf(alias, this);
    }

    @Override
    public NotenPdf as(Table<?> alias) {
        return new NotenPdf(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public NotenPdf rename(String name) {
        return new NotenPdf(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public NotenPdf rename(Name name) {
        return new NotenPdf(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public NotenPdf rename(Table<?> name) {
        return new NotenPdf(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public NotenPdf where(Condition condition) {
        return new NotenPdf(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public NotenPdf where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public NotenPdf where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public NotenPdf where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public NotenPdf where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public NotenPdf where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public NotenPdf where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public NotenPdf where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public NotenPdf whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public NotenPdf whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
