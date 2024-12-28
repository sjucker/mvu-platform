package ch.mvurdorf.platform.passivmitglied;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;

import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;

@PageTitle("Passivmitglied")

@Route(value = "portal", autoLayout = false)
@AnonymousAllowed
public class PassivmitgliedPortalView extends AppLayout implements HasUrlParameter<String> {

    private final PassivmitgliedService passivmitgliedService;

    public PassivmitgliedPortalView(PassivmitgliedService passivmitgliedService) {
        this.passivmitgliedService = passivmitgliedService;

        var title = new H3("Passivmitglied");
        title.addClassName(Padding.SMALL);
        addToNavbar(title);
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        var passivmitglied = passivmitgliedService.findByUUID(parameter).orElse(null);
        if (passivmitglied != null) {
            var tabs = new TabSheet();
            tabs.setSizeFull();
            tabs.add(new Tab("Daten"), createDatenTabs(passivmitglied));
            setContent(tabs);
        } else {
            setContent(new VerticalLayout(new Paragraph("Passivmitglied nicht gefunden.")));
        }
    }

    private Component createDatenTabs(PassivmitgliedDto passivmitglied) {
        var content = new VerticalLayout();
        content.setSizeFull();

        var binder = new Binder<>(PassivmitgliedEditDTO.class);

        var formLayout = new FormLayout();

        var anrede = new Select<String>();
        anrede.setItems("Liebe", "Lieber", "Hallo");
        binder.bind(anrede, PassivmitgliedEditDTO::getAnrede, PassivmitgliedEditDTO::setAnrede);
        formLayout.setColspan(formLayout.addFormItem(anrede, "Anrede"), 2);

        var vorname = new TextField();
        binder.forField(vorname)
              .withValidator(new StringLengthValidator("Vorname muss zwischen 1 und 255 Zeichen haben", 1, 255))
              .bind(PassivmitgliedEditDTO::getVorname, PassivmitgliedEditDTO::setVorname);
        formLayout.addFormItem(vorname, "Vorname");

        var nachname = new TextField();
        binder.forField(nachname)
              .withValidator(new StringLengthValidator("Nachname muss zwischen 1 und 255 Zeichen haben", 1, 255))
              .bind(PassivmitgliedEditDTO::getNachname, PassivmitgliedEditDTO::setNachname);
        formLayout.addFormItem(nachname, "Nachname");

        var strasse = new TextField();
        binder.forField(strasse)
              .withValidator(new StringLengthValidator("Strasse muss zwischen 1 und 255 Zeichen haben", 1, 255))
              .bind(PassivmitgliedEditDTO::getStrasse, PassivmitgliedEditDTO::setStrasse);
        formLayout.addFormItem(strasse, "Strasse");

        var ort = new TextField();
        binder.forField(ort)
              .withValidator(new StringLengthValidator("PLZ/Ort muss zwischen 1 und 255 Zeichen haben", 1, 255))
              .bind(PassivmitgliedEditDTO::getOrt, PassivmitgliedEditDTO::setOrt);
        formLayout.addFormItem(ort, "PLZ/Ort");

        var email = new TextField();
        binder.forField(email)
              .withValidator(new EmailValidator("Valide E-Mail eingeben"))
              .withValidator(new StringLengthValidator("PLZ/Ort muss zwischen 1 und 255 Zeichen haben", 1, 255))
              .bind(PassivmitgliedEditDTO::getEmail, PassivmitgliedEditDTO::setEmail);
        formLayout.setColspan(formLayout.addFormItem(email, "E-Mail"), 2);

        var kommunikationPost = new Checkbox();
        binder.bind(kommunikationPost, PassivmitgliedEditDTO::isKommunikationPost, PassivmitgliedEditDTO::setKommunikationPost);
        formLayout.addFormItem(kommunikationPost, "Kommunikation per Post");

        var kommunikationEmail = new Checkbox();
        binder.bind(kommunikationEmail, PassivmitgliedEditDTO::isKommunikationEmail, PassivmitgliedEditDTO::setKommunikationEmail);
        formLayout.addFormItem(kommunikationEmail, "Kommunikation per E-Mail");

        var edit = passivmitglied.edit();
        binder.readBean(edit);
        content.add(formLayout);
        var save = primaryButton("Speichern", () -> {
            if (binder.writeBeanIfValid(edit)) {
                passivmitgliedService.update(edit);
                Notification.show("Gespeichert!");
            } else {
                Notification.show("Bitte alle Felder ausfüllen");
            }
        });
        save.setDisableOnClick(true);
        save.setEnabled(false);
        binder.addValueChangeListener(_ -> save.setEnabled(true));
        content.add(save);

        return content;
    }

}