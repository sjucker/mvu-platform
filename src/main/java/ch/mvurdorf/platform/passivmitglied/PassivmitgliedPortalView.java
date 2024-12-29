package ch.mvurdorf.platform.passivmitglied;

import ch.mvurdorf.platform.utils.DateUtil;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
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
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;

import java.io.ByteArrayInputStream;

import static ch.mvurdorf.platform.ui.ComponentUtil.image;
import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static ch.mvurdorf.platform.ui.RendererUtil.dateRenderer;

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
            tabs.add(new Tab("Bezahlen"), createBezahlenTab(passivmitglied));
            tabs.add(new Tab("Gutscheine"), createVouchersTab(passivmitglied));
            tabs.add(new Tab("Daten"), createDatenTab(passivmitglied));
            setContent(tabs);
        } else {
            setContent(new VerticalLayout(new Paragraph("Passivmitglied nicht gefunden.")));
        }
    }

    private Component createBezahlenTab(PassivmitgliedDto passivmitglied) {
        var content = new VerticalLayout();
        content.setSizeFull();

        var qrBillImage = new Image(new StreamResource("qr-bill.png", () -> {
            var imgBytes = passivmitgliedService.qrBill(passivmitglied.externalId()).orElseThrow();
            return new ByteArrayInputStream(imgBytes);
        }), "QR Rechnung");
        qrBillImage.addClassName(MaxWidth.FULL);
        content.add(qrBillImage);

        if (!passivmitglied.payments().isEmpty()) {
            content.add(new H3("Vergangene Bezahlungen"));
            content.add(new UnorderedList(passivmitglied.paymentsNewestFirst().stream()
                                                        .map(it -> new ListItem(it.description()))
                                                        .toArray(ListItem[]::new)));

        }

        return content;
    }

    private Component createVouchersTab(PassivmitgliedDto passivmitglied) {
        var content = new VerticalLayout();
        content.setSizeFull();

        var grid = new Grid<PassivmitgliedVoucherDto>();
        grid.setSizeFull();
        grid.addColumn(PassivmitgliedVoucherDto::description).setHeader("Titel");
        grid.addColumn(dateRenderer(PassivmitgliedVoucherDto::validUntil)).setHeader("Gültig bis");
        grid.addColumn(PassivmitgliedVoucherDto::code).setHeader("Code");
        grid.addComponentColumn(image(PassivmitgliedVoucherDto::code, dto -> passivmitgliedService.qrCode(passivmitglied.externalId(), dto)))
            .setHeader("QR-Code");

        grid.setItems(passivmitglied.vouchersNewestFirst().stream()
                                    .filter(v -> v.validUntil().isAfter(DateUtil.today()))
                                    .toList());

        content.add(grid);
        return content;
    }

    private Component createDatenTab(PassivmitgliedDto passivmitglied) {
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

        var strasseNr = new TextField();
        binder.forField(strasseNr)
              .withValidator(new StringLengthValidator("Nr. muss zwischen 1 und 5 Zeichen haben", 1, 5))
              .bind(PassivmitgliedEditDTO::getStrasseNr, PassivmitgliedEditDTO::setStrasseNr);
        formLayout.addFormItem(strasseNr, "Nr.");

        var plz = new TextField();
        binder.forField(plz)
              .withValidator(new StringLengthValidator("PLZ muss zwischen 1 und 10 Zeichen haben", 1, 10))
              .bind(PassivmitgliedEditDTO::getPlz, PassivmitgliedEditDTO::setPlz);
        formLayout.addFormItem(plz, "PLZ");

        var ort = new TextField();
        binder.forField(ort)
              .withValidator(new StringLengthValidator("Ort muss zwischen 1 und 255 Zeichen haben", 1, 255))
              .bind(PassivmitgliedEditDTO::getOrt, PassivmitgliedEditDTO::setOrt);
        formLayout.addFormItem(ort, "Ort");

        var email = new TextField();
        binder.forField(email)
              .withValidator(new EmailValidator("Valide E-Mail eingeben"))
              .withValidator(new StringLengthValidator("E-Mail muss zwischen 1 und 255 Zeichen haben", 1, 255))
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
