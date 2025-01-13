package ch.mvurdorf.platform.supporter;

import ch.mvurdorf.platform.utils.DateUtil;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
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

@PageTitle("Supporter")
@Route(value = "portal", autoLayout = false)
@AnonymousAllowed
public class SupporterPortalView extends AppLayout implements HasUrlParameter<String> {

    private final SupporterService supporterService;

    public SupporterPortalView(SupporterService supporterService) {
        this.supporterService = supporterService;

        var title = new H3("Supporter");
        title.addClassName(Padding.SMALL);
        addToNavbar(title);
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        var supporter = supporterService.findByUUID(parameter).orElse(null);
        if (supporter != null) {
            var tabs = new TabSheet();
            tabs.setSizeFull();
            tabs.add(new Tab("Bezahlen"), createBezahlenTab(supporter));
            tabs.add(new Tab("Gutscheine"), createVouchersTab(supporter));
            tabs.add(new Tab("Daten"), createDatenTab(supporter));
            setContent(tabs);
        } else {
            setContent(new VerticalLayout(new Paragraph("Supporter nicht gefunden.")));
        }
    }

    private Component createBezahlenTab(SupporterDto supporter) {
        var content = new VerticalLayout();
        content.setSizeFull();

        content.add(new Anchor(new StreamResource("qr-rechnung.pdf", () -> {
            var pdfBytes = supporterService.qrBillPdf(supporter.externalId()).orElseThrow();
            return new ByteArrayInputStream(pdfBytes);
        }), "Rechnung als PDF herunterladen"));

        var qrBillImage = new Image(new StreamResource("qr-bill.png", () -> {
            var imgBytes = supporterService.qrBill(supporter.externalId()).orElseThrow();
            return new ByteArrayInputStream(imgBytes);
        }), "QR Rechnung");
        qrBillImage.addClassName(MaxWidth.FULL);
        content.add(qrBillImage);

        if (!supporter.payments().isEmpty()) {
            content.add(new H3("Vergangene Bezahlungen"));
            content.add(new UnorderedList(supporter.paymentsNewestFirst().stream()
                                                   .map(it -> new ListItem(it.description()))
                                                   .toArray(ListItem[]::new)));

        }

        return content;
    }

    private Component createVouchersTab(SupporterDto supporter) {
        var content = new VerticalLayout();
        content.setSizeFull();

        var grid = new Grid<SupporterVoucherDto>();
        grid.setSizeFull();
        grid.addColumn(SupporterVoucherDto::description).setHeader("Titel");
        grid.addColumn(dateRenderer(SupporterVoucherDto::validUntil)).setHeader("Gültig bis");
        grid.addColumn(SupporterVoucherDto::code).setHeader("Code");
        grid.addComponentColumn(image(SupporterVoucherDto::code, dto -> supporterService.qrCode(supporter.externalId(), dto)))
            .setHeader("QR-Code");

        grid.setItems(supporter.vouchersNewestFirst().stream()
                               .filter(v -> !DateUtil.today().isAfter(v.validUntil()))
                               .toList());

        content.add(grid);
        return content;
    }

    private Component createDatenTab(SupporterDto supporter) {
        var content = new VerticalLayout();
        content.setSizeFull();

        var binder = new Binder<>(SupporterEditDTO.class);

        var formLayout = new FormLayout();

        var anrede = new Select<String>();
        anrede.setItems("Liebe", "Lieber", "Hallo");
        binder.bind(anrede, SupporterEditDTO::getAnrede, SupporterEditDTO::setAnrede);
        formLayout.setColspan(formLayout.addFormItem(anrede, "Anrede"), 2);

        var vorname = new TextField();
        binder.forField(vorname)
              .withValidator(new StringLengthValidator("Vorname muss zwischen 1 und 255 Zeichen haben", 1, 255))
              .bind(SupporterEditDTO::getVorname, SupporterEditDTO::setVorname);
        formLayout.addFormItem(vorname, "Vorname");

        var nachname = new TextField();
        binder.forField(nachname)
              .withValidator(new StringLengthValidator("Nachname muss zwischen 1 und 255 Zeichen haben", 1, 255))
              .bind(SupporterEditDTO::getNachname, SupporterEditDTO::setNachname);
        formLayout.addFormItem(nachname, "Nachname");

        var strasse = new TextField();
        binder.forField(strasse)
              .withValidator(new StringLengthValidator("Strasse muss zwischen 1 und 255 Zeichen haben", 1, 255))
              .bind(SupporterEditDTO::getStrasse, SupporterEditDTO::setStrasse);
        formLayout.addFormItem(strasse, "Strasse");

        var strasseNr = new TextField();
        binder.forField(strasseNr)
              .withValidator(new StringLengthValidator("Nr. muss zwischen 1 und 5 Zeichen haben", 1, 5))
              .bind(SupporterEditDTO::getStrasseNr, SupporterEditDTO::setStrasseNr);
        formLayout.addFormItem(strasseNr, "Nr.");

        var plz = new TextField();
        binder.forField(plz)
              .withValidator(new StringLengthValidator("PLZ muss zwischen 1 und 10 Zeichen haben", 1, 10))
              .bind(SupporterEditDTO::getPlz, SupporterEditDTO::setPlz);
        formLayout.addFormItem(plz, "PLZ");

        var ort = new TextField();
        binder.forField(ort)
              .withValidator(new StringLengthValidator("Ort muss zwischen 1 und 255 Zeichen haben", 1, 255))
              .bind(SupporterEditDTO::getOrt, SupporterEditDTO::setOrt);
        formLayout.addFormItem(ort, "Ort");

        var email = new TextField();
        binder.forField(email)
              .withValidator(new EmailValidator("Valide E-Mail eingeben"))
              .withValidator(new StringLengthValidator("E-Mail muss zwischen 1 und 255 Zeichen haben", 1, 255))
              .bind(SupporterEditDTO::getEmail, SupporterEditDTO::setEmail);
        formLayout.setColspan(formLayout.addFormItem(email, "E-Mail"), 2);

        var kommunikationPost = new Checkbox();
        binder.bind(kommunikationPost, SupporterEditDTO::isKommunikationPost, SupporterEditDTO::setKommunikationPost);
        formLayout.addFormItem(kommunikationPost, "Kommunikation per Post");

        var kommunikationEmail = new Checkbox();
        binder.bind(kommunikationEmail, SupporterEditDTO::isKommunikationEmail, SupporterEditDTO::setKommunikationEmail);
        formLayout.addFormItem(kommunikationEmail, "Kommunikation per E-Mail");

        var edit = supporter.edit();
        binder.readBean(edit);
        content.add(formLayout);
        var save = primaryButton("Speichern", () -> {
            if (binder.writeBeanIfValid(edit)) {
                supporterService.update(edit);
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
