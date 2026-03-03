package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.Application.PlatformProperties;
import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.ui.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouteConfiguration;

import java.util.List;

import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static ch.mvurdorf.platform.ui.ComponentUtil.secondaryButton;
import static ch.mvurdorf.platform.ui.ComponentUtil.tertiaryButton;
import static com.vaadin.flow.component.icon.VaadinIcon.COPY;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END;
import static com.vaadin.flow.theme.lumo.LumoUtility.FontWeight.BOLD;
import static com.vaadin.flow.theme.lumo.LumoUtility.TextColor.WARNING;

public class ShareableLinkDialog extends Dialog {

    private final ShareableLinkService shareableLinkService;
    private final List<KompositionDto> selectedKompositionen;
    private final PlatformProperties platformProperties;

    private final TextField description;
    private final DatePicker validUntil;
    private final MultiSelectComboBox<Instrument> instrumentSelect;

    private ShareableLinkDialog(ShareableLinkService shareableLinkService, List<KompositionDto> selectedKompositionen, PlatformProperties platformProperties) {
        this.shareableLinkService = shareableLinkService;
        this.selectedKompositionen = selectedKompositionen;
        this.platformProperties = platformProperties;

        setHeaderTitle("Freigabe-Link erstellen (%d Kompositionen)".formatted(selectedKompositionen.size()));

        description = new TextField("Beschreibung");
        description.setRequired(true);
        description.setWidthFull();

        validUntil = ComponentUtil.datePicker("Gültig bis");
        validUntil.setRequired(true);
        validUntil.setWidthFull();

        instrumentSelect = new MultiSelectComboBox<>("Instrumente");
        instrumentSelect.setItems(Instrument.values());
        instrumentSelect.setItemLabelGenerator(Instrument::getDescription);
        instrumentSelect.setRequired(true);
        instrumentSelect.setWidthFull();

        var layout = new VerticalLayout(description, validUntil, instrumentSelect);
        if (selectedKompositionen.stream().anyMatch(dto -> dto.notenCount() == 0)) {
            var warning = new Paragraph("Achtung: mindestens eine Komposition hat keine Noten hochgeladen.");
            warning.addClassNames(WARNING, BOLD);
            layout.add(warning);
        }
        add(layout);

        var cancelButton = secondaryButton("Abbrechen", this::close);
        var createButton = primaryButton("Erstellen", this::create);
        getFooter().add(cancelButton, createButton);
    }

    private void create() {
        if (instrumentSelect.getValue().isEmpty()) {
            Notification.show("Mindestens ein Instrument muss ausgewählt sein.");
            return;
        }

        if (validUntil.getValue() == null) {
            Notification.show("Das Ablaufdatum darf nicht leer sein.");
            return;
        }

        var dto = new ShareableLinkDto(description.getValue(),
                                       validUntil.getValue(),
                                       selectedKompositionen,
                                       instrumentSelect.getValue());

        var uuid = shareableLinkService.createShareableLink(dto);
        var url = platformProperties.supportUrl() + "/" + RouteConfiguration.forSessionScope().getUrl(SharedNotenView.class, uuid);

        var resultDialog = new Dialog();
        resultDialog.setHeaderTitle("Link erstellt");
        var urlField = new TextField("Freigabe-URL");
        urlField.setValue(url);
        urlField.setReadOnly(true);
        urlField.setWidthFull();

        var copyButton = new Button(COPY.create(), _ -> {
            urlField.getElement().executeJs("navigator.clipboard.writeText($0)", urlField.getValue());
            Notification.show("In die Zwischenablage kopiert");
        });
        copyButton.setTooltipText("In die Zwischenablage kopieren");

        var urlLayout = new HorizontalLayout(urlField, copyButton);
        urlLayout.setVerticalComponentAlignment(END, copyButton);
        urlLayout.setWidthFull();

        resultDialog.add(new VerticalLayout(urlLayout));
        resultDialog.setWidth("500px");
        resultDialog.getFooter().add(tertiaryButton("Schliessen", resultDialog::close));
        resultDialog.open();

        close();
    }

    public static void show(ShareableLinkService shareableLinkService,
                            PlatformProperties platformProperties,
                            List<KompositionDto> selectedKompositionen) {
        new ShareableLinkDialog(shareableLinkService, selectedKompositionen, platformProperties).open();
    }
}
