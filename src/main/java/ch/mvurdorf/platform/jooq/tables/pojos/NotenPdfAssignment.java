/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables.pojos;


import java.io.Serializable;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class NotenPdfAssignment implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long fkNotenPdf;
    private String instrument;
    private String stimme;

    public NotenPdfAssignment() {}

    public NotenPdfAssignment(NotenPdfAssignment value) {
        this.id = value.id;
        this.fkNotenPdf = value.fkNotenPdf;
        this.instrument = value.instrument;
        this.stimme = value.stimme;
    }

    public NotenPdfAssignment(
        Long id,
        Long fkNotenPdf,
        String instrument,
        String stimme
    ) {
        this.id = id;
        this.fkNotenPdf = fkNotenPdf;
        this.instrument = instrument;
        this.stimme = stimme;
    }

    /**
     * Getter for <code>public.noten_pdf_assignment.id</code>.
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for <code>public.noten_pdf_assignment.id</code>.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Getter for <code>public.noten_pdf_assignment.fk_noten_pdf</code>.
     */
    public Long getFkNotenPdf() {
        return this.fkNotenPdf;
    }

    /**
     * Setter for <code>public.noten_pdf_assignment.fk_noten_pdf</code>.
     */
    public void setFkNotenPdf(Long fkNotenPdf) {
        this.fkNotenPdf = fkNotenPdf;
    }

    /**
     * Getter for <code>public.noten_pdf_assignment.instrument</code>.
     */
    public String getInstrument() {
        return this.instrument;
    }

    /**
     * Setter for <code>public.noten_pdf_assignment.instrument</code>.
     */
    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    /**
     * Getter for <code>public.noten_pdf_assignment.stimme</code>.
     */
    public String getStimme() {
        return this.stimme;
    }

    /**
     * Setter for <code>public.noten_pdf_assignment.stimme</code>.
     */
    public void setStimme(String stimme) {
        this.stimme = stimme;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final NotenPdfAssignment other = (NotenPdfAssignment) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.fkNotenPdf == null) {
            if (other.fkNotenPdf != null)
                return false;
        }
        else if (!this.fkNotenPdf.equals(other.fkNotenPdf))
            return false;
        if (this.instrument == null) {
            if (other.instrument != null)
                return false;
        }
        else if (!this.instrument.equals(other.instrument))
            return false;
        if (this.stimme == null) {
            if (other.stimme != null)
                return false;
        }
        else if (!this.stimme.equals(other.stimme))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.fkNotenPdf == null) ? 0 : this.fkNotenPdf.hashCode());
        result = prime * result + ((this.instrument == null) ? 0 : this.instrument.hashCode());
        result = prime * result + ((this.stimme == null) ? 0 : this.stimme.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NotenPdfAssignment (");

        sb.append(id);
        sb.append(", ").append(fkNotenPdf);
        sb.append(", ").append(instrument);
        sb.append(", ").append(stimme);

        sb.append(")");
        return sb.toString();
    }
}
