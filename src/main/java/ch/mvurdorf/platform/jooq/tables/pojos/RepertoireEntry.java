/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq.tables.pojos;


import java.io.Serializable;
import java.math.BigDecimal;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class RepertoireEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long fkRepertoire;
    private Long fkKomposition;
    private BigDecimal number;

    public RepertoireEntry() {}

    public RepertoireEntry(RepertoireEntry value) {
        this.id = value.id;
        this.fkRepertoire = value.fkRepertoire;
        this.fkKomposition = value.fkKomposition;
        this.number = value.number;
    }

    public RepertoireEntry(
        Long id,
        Long fkRepertoire,
        Long fkKomposition,
        BigDecimal number
    ) {
        this.id = id;
        this.fkRepertoire = fkRepertoire;
        this.fkKomposition = fkKomposition;
        this.number = number;
    }

    /**
     * Getter for <code>public.repertoire_entry.id</code>.
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for <code>public.repertoire_entry.id</code>.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Getter for <code>public.repertoire_entry.fk_repertoire</code>.
     */
    public Long getFkRepertoire() {
        return this.fkRepertoire;
    }

    /**
     * Setter for <code>public.repertoire_entry.fk_repertoire</code>.
     */
    public void setFkRepertoire(Long fkRepertoire) {
        this.fkRepertoire = fkRepertoire;
    }

    /**
     * Getter for <code>public.repertoire_entry.fk_komposition</code>.
     */
    public Long getFkKomposition() {
        return this.fkKomposition;
    }

    /**
     * Setter for <code>public.repertoire_entry.fk_komposition</code>.
     */
    public void setFkKomposition(Long fkKomposition) {
        this.fkKomposition = fkKomposition;
    }

    /**
     * Getter for <code>public.repertoire_entry.number</code>.
     */
    public BigDecimal getNumber() {
        return this.number;
    }

    /**
     * Setter for <code>public.repertoire_entry.number</code>.
     */
    public void setNumber(BigDecimal number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final RepertoireEntry other = (RepertoireEntry) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.fkRepertoire == null) {
            if (other.fkRepertoire != null)
                return false;
        }
        else if (!this.fkRepertoire.equals(other.fkRepertoire))
            return false;
        if (this.fkKomposition == null) {
            if (other.fkKomposition != null)
                return false;
        }
        else if (!this.fkKomposition.equals(other.fkKomposition))
            return false;
        if (this.number == null) {
            if (other.number != null)
                return false;
        }
        else if (!this.number.equals(other.number))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.fkRepertoire == null) ? 0 : this.fkRepertoire.hashCode());
        result = prime * result + ((this.fkKomposition == null) ? 0 : this.fkKomposition.hashCode());
        result = prime * result + ((this.number == null) ? 0 : this.number.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RepertoireEntry (");

        sb.append(id);
        sb.append(", ").append(fkRepertoire);
        sb.append(", ").append(fkKomposition);
        sb.append(", ").append(number);

        sb.append(")");
        return sb.toString();
    }
}
