package ch.mvurdorf.platform.noten;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.Consumer;

import static ch.mvurdorf.platform.noten.NotenFormat.KONZERTMAPPE;
import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static com.vaadin.flow.component.Unit.PERCENTAGE;
import static java.util.Optional.ofNullable;

public class KompositionDialog extends Dialog {

    private final Consumer<KompositionDto> callback;

    private KompositionDialog(Consumer<KompositionDto> callback) {
        this.callback = callback;
    }

    public static void show(KompositionDto existingKomposition, Consumer<KompositionDto> callback) {
        var dialog = new KompositionDialog(callback);
        dialog.init(existingKomposition);
        dialog.setModal(true);
        dialog.setMinWidth(66, PERCENTAGE);
        dialog.open();
    }

    public static void show(Consumer<KompositionDto> callback) {
        var dialog = new KompositionDialog(callback);
        dialog.init(null);
        dialog.setModal(true);
        dialog.setMinWidth(66, PERCENTAGE);
        dialog.open();
    }

    private void init(KompositionDto existingKomposition) {
        setModal(true);
        setCloseOnOutsideClick(false);

        var formLayout = new FormLayout();
        var titel = new TextField("Titel");
        titel.setRequired(true);
        formLayout.add(titel, 2);

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

        var audioSample = new TextField("Hörprobe (URL)");
        formLayout.add(audioSample);

        var comment = new TextArea("Kommentar");
        formLayout.add(comment, 2);

        if (existingKomposition != null) {
            titel.setValue(existingKomposition.titel());
            ofNullable(existingKomposition.komponist()).ifPresent(komponist::setValue);
            ofNullable(existingKomposition.arrangeur()).ifPresent(arrangeur::setValue);
            format.setValue(existingKomposition.format());
            ofNullable(existingKomposition.audioSample()).ifPresent(audioSample::setValue);
            ofNullable(existingKomposition.comment()).ifPresent(comment::setValue);
        }

        add(formLayout);

        getFooter().add(new Button("Abbrechen", _ -> close()),
                        primaryButton("Speichern", () -> {
                            callback.accept(new KompositionDto(existingKomposition != null ? existingKomposition.id() : null,
                                                               titel.getValue(),
                                                               komponist.getOptionalValue().orElse(null),
                                                               arrangeur.getOptionalValue().orElse(null),
                                                               format.getValue(),
                                                               audioSample.getOptionalValue().orElse(null),
                                                               comment.getOptionalValue().orElse(null)));
                            close();
                        }));
    }
}
