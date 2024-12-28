package ch.mvurdorf.platform.passivmitglied;

import ch.mvurdorf.platform.jooq.tables.pojos.Passivmitglied;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PassivmitgliedEditDTO {
    private final Long id;
    private String anrede;
    private String vorname;
    private String nachname;
    private String strasse;
    private String ort;
    private String email;
    private String bemerkung;
    private boolean kommunikationPost;
    private boolean kommunikationEmail;

    public void applyTo(Passivmitglied existing) {
        existing.setAnrede(anrede);
        existing.setVorname(vorname);
        existing.setNachname(nachname);
        existing.setStrasse(strasse);
        existing.setOrt(ort);
        existing.setEmail(email);
        existing.setBemerkung(bemerkung);
        existing.setKommunikationPost(kommunikationPost);
        existing.setKommunikationEmail(kommunikationEmail);
    }
}