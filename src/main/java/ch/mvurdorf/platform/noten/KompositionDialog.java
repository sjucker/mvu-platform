package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.utils.DurationUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.Consumer;

public class KompositionDialog extends Dialog {

    private final Consumer<KompositionDto> callback;

    public KompositionDialog(Consumer<KompositionDto> callback) {
        this.callback = callback;
    }

    public static void show(Consumer<KompositionDto> callback) {
        var dialog = new KompositionDialog(callback);
        dialog.init();
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

        var duration = new TextField("Dauer (mm:ss)");
        formLayout.add(duration);

        add(formLayout);

        getFooter().add(new Button("Abbrechen", e -> close()),
                        new Button("Speichern", e -> {
                            callback.accept(new KompositionDto(null,
                                                               titel.getValue(),
                                                               komponist.getOptionalValue().orElse(null),
                                                               arrangeur.getOptionalValue().orElse(null),
                                                               DurationUtil.toDurationInSeconds(duration.getValue()).orElse(null)));
                            close();
                        }));
    }
}
