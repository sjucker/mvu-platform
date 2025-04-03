package ch.mvurdorf.platform.supporter;

import ch.mvurdorf.platform.jooq.tables.pojos.Supporter;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SupporterEditDto {
    private final Long id;
    private String anrede;
    private String vorname;
    private String nachname;
    private String strasse;
    private String strasseNr;
    private String plz;
    private String ort;
    private String email;
    private String bemerkung;
    private boolean kommunikationPost;
    private boolean kommunikationEmail;

    public void applyTo(Supporter existing) {
        existing.setAnrede(anrede);
        existing.setVorname(vorname);
        existing.setNachname(nachname);
        existing.setStrasse(strasse);
        existing.setStrasseNr(strasseNr);
        existing.setPlz(plz);
        existing.setOrt(ort);
        existing.setEmail(email);
        existing.setBemerkung(bemerkung);
        existing.setKommunikationPost(kommunikationPost);
        existing.setKommunikationEmail(kommunikationEmail);
    }
}
