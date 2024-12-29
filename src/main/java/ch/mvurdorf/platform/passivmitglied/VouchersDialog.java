package ch.mvurdorf.platform.passivmitglied;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.function.BooleanConsumer;

import static ch.mvurdorf.platform.ui.ComponentUtil.datePicker;
import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;

public class VouchersDialog extends Dialog {

    private final PassivmitgliedService service;

    private VouchersDialog(PassivmitgliedService service) {
        this.service = service;
    }

    public static void show(PassivmitgliedService service, BooleanConsumer callback) {
        var dialog = new VouchersDialog(service);
        dialog.init(callback);
        dialog.setModal(true);
        dialog.setResizable(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setMinWidth("66%");
        dialog.open();
    }

    private void init(BooleanConsumer callback) {
        setHeaderTitle("Gutscheine erstellen");
        var formLayout = new FormLayout();
        var prefix = new TextField("Code-Prefix");
        formLayout.add(prefix);
        var description = new TextField("Beschreibung");
        formLayout.add(description);
        var validUntil = datePicker("Gültig bis");
        formLayout.add(validUntil);

        getFooter().add(new Button("Abbrechen", _ -> close()));
        getFooter().add(primaryButton("Speichern", () -> {
            service.createVouchers(prefix.getValue(), description.getValue(), validUntil.getValue());
            callback.accept(true);
            close();
        }));

        add(formLayout);
    }
}
