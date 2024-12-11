package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Stimme;
import ch.mvurdorf.platform.common.Stimmlage;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

import java.io.IOException;

public class NotenDialog extends Dialog {

    private final NotenService notenService;

    private Button save;

    private NotenDialog(NotenService notenService) {
        this.notenService = notenService;
    }

    public static void show(NotenService notenService, KompositionDto komposition) {
        var notenDialog = new NotenDialog(notenService);
        notenDialog.init(komposition);
        notenDialog.open();
    }

    private void init(KompositionDto komposition) {
        setHeaderTitle(komposition.titel());

        var left = new VerticalLayout();
        // TODO add grid with alrady uploaded noten

        var right = new VerticalLayout();

        var buffer = new MemoryBuffer();
        var upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".pdf");
        upload.addSucceededListener(event -> save.setEnabled(true));

        right.add(upload);

        var instrument = new ComboBox<Instrument>("Instrument");
        instrument.setItems(Instrument.values());
        instrument.setItemLabelGenerator(Instrument::getDescription);
        instrument.setRequired(true);
        right.add(instrument);

        var stimme = new ComboBox<Stimme>("Stimme");
        stimme.setItems(Stimme.values());
        stimme.setItemLabelGenerator(Stimme::getDescription);
        right.add(stimme);

        var stimmlage = new ComboBox<Stimmlage>("Stimmlage");
        stimmlage.setItems(Stimmlage.values());
        stimmlage.setItemLabelGenerator(Stimmlage::getDescription);
        right.add(stimmlage);

         save = new Button("Speichern", event -> {
            try {
                notenService.insert(komposition.id(),
                                    new NotenDto(instrument.getValue(),
                                                 stimme.getOptionalValue().orElse(null),
                                                 stimmlage.getOptionalValue().orElse(null)),
                                    buffer.getInputStream().readAllBytes());

                upload.clearFileList();

                // TODO reload grid
            } catch (IOException e) {
                Notification.show("Ein Fehler ist aufgetreten");
            }
        });
        save.setDisableOnClick(true);
        right.add(save);

        add(new SplitLayout(left, right));

        getFooter().add(new Button("Schliessen", e -> close()));
    }
}
