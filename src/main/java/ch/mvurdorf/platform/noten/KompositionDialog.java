package ch.mvurdorf.platform.noten;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.Consumer;

import static ch.mvurdorf.platform.noten.NotenFormat.KONZERTMAPPE;
import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static com.vaadin.flow.component.Unit.PERCENTAGE;

public class KompositionDialog extends Dialog {

    private final Consumer<KompositionDto> callback;

    private KompositionDialog(Consumer<KompositionDto> callback) {
        this.callback = callback;
    }

    public static void show(Consumer<KompositionDto> callback) {
        var dialog = new KompositionDialog(callback);
        dialog.init();
        dialog.setModal(true);
        dialog.setMinWidth(66, PERCENTAGE);
        dialog.open();
    }

    private void init() {
        setModal(true);
        setCloseOnOutsideClick(false);

        var formLayout = new FormLayout();
        var titel = new TextField("Titel");
        titel.setRequired(true);
        formLayout.add(titel);

        var komponist = new TextField("Komponist");
        formLayout.add(komponist);

        var arrangeur = new TextField("Arrangeur");
        formLayout.add(arrangeur);

        var format = new Select<NotenFormat>();
        format.setLabel("Format");
        format.setItems(NotenFormat.values());
        format.setValue(KONZERTMAPPE);
        format.setItemLabelGenerator(NotenFormat::getDescription);
        formLayout.add(format);

        add(formLayout);

        getFooter().add(new Button("Abbrechen", _ -> close()),
                        primaryButton("Speichern", () -> {
                            callback.accept(new KompositionDto(null,
                                                               titel.getValue(),
                                                               komponist.getOptionalValue().orElse(null),
                                                               arrangeur.getOptionalValue().orElse(null),
                                                               format.getValue()));
                            close();
                        }));
    }
}
