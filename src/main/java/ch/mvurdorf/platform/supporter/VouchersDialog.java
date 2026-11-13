package ch.mvurdorf.platform.supporter;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.function.BooleanConsumer;

import static ch.mvurdorf.platform.supporter.SupporterType.PASSIVMITGLIED;
import static ch.mvurdorf.platform.ui.ComponentUtil.datePicker;
import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static com.vaadin.flow.component.ModalityMode.STRICT;

public class VouchersDialog extends Dialog {

    private final SupporterService service;

    private VouchersDialog(SupporterService service) {
        this.service = service;
    }

    public static void show(SupporterService service, BooleanConsumer callback) {
        var dialog = new VouchersDialog(service);
        dialog.init(callback);
        dialog.setModality(STRICT);
        dialog.setResizable(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setMinWidth("66%");
        dialog.open();
    }

    private void init(BooleanConsumer callback) {
        setHeaderTitle("Gutscheine erstellen");
        var formLayout = new FormLayout();
        var type = new Select<SupporterType>();
        type.setItems(SupporterType.values());
        type.setItemLabelGenerator(SupporterType::getDescription);
        type.setValue(PASSIVMITGLIED);
        formLayout.add(type);

        var prefix = new TextField("Code-Prefix");
        formLayout.add(prefix);
        var description = new TextField("Beschreibung");
        formLayout.add(description);
        var validUntil = datePicker("Gültig bis");
        formLayout.add(validUntil);
        var paidWithinMonths = new IntegerField("Nur bei Bezahlung in den letzten Monaten");
        paidWithinMonths.setMin(1);
        paidWithinMonths.setClearButtonVisible(true);
        paidWithinMonths.setPlaceholder("Alle");
        paidWithinMonths.setHelperText("Optional, z.B. 12 = nur sichtbar, wer in den letzten 12 Monaten bezahlt hat");
        formLayout.add(paidWithinMonths);

        getFooter().add(new Button("Abbrechen", _ -> close()));
        getFooter().add(primaryButton("Speichern", () -> {
            if (paidWithinMonths.isInvalid()) {
                return;
            }
            service.createVouchers(prefix.getValue(), description.getValue(), validUntil.getValue(), type.getValue(),
                                   paidWithinMonths.getValue());
            callback.accept(true);
            close();
        }));

        add(formLayout);
    }
}
