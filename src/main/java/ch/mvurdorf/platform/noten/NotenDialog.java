package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Stimme;
import ch.mvurdorf.platform.common.Stimmlage;
import ch.mvurdorf.platform.service.StorageService;
import ch.mvurdorf.platform.ui.LocalizedEnumRenderer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static ch.mvurdorf.platform.ui.RendererUtil.iconDownloadLink;
import static com.vaadin.flow.component.Unit.PERCENTAGE;
import static com.vaadin.flow.component.icon.VaadinIcon.DOWNLOAD;

@RequiredArgsConstructor
public class NotenDialog extends Dialog {

    private final NotenService notenService;
    private final StorageService storageService;

    private Button save;

    public static void show(NotenService notenService, StorageService storageService, KompositionDto komposition) {
        var dialog = new NotenDialog(notenService, storageService);
        dialog.init(komposition);
        dialog.setModal(true);
        dialog.setMinWidth(66, PERCENTAGE);
        dialog.open();
    }

    private void init(KompositionDto komposition) {
        setHeaderTitle(komposition.titel());

        var left = new VerticalLayout();
        var grid = new Grid<NotenDto>();
        grid.setSizeFull();
        grid.addColumn(new LocalizedEnumRenderer<>(NotenDto::instrument)).setHeader("Instrument");
        grid.addColumn(new LocalizedEnumRenderer<>(NotenDto::stimme)).setHeader("Stimme");
        grid.addColumn(new LocalizedEnumRenderer<>(NotenDto::stimmlage)).setHeader("Stimmlage");
        grid.addColumn(iconDownloadLink(DOWNLOAD, dto -> storageService.read(dto.id()), dto -> "%s.pdf".formatted(dto.instrument().getDescription())));
        grid.setItems(notenService.findByKomposition(komposition.id()));

        left.add(grid);

        var right = new VerticalLayout();

        var buffer = new MemoryBuffer();
        var upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".pdf");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(1024 * 1024 * 50);
        upload.addSucceededListener(event -> save.setEnabled(true));

        right.add(upload);

        var instrument = new ComboBox<Instrument>("Instrument");
        instrument.setItems(Instrument.values());
        instrument.setItemLabelGenerator(Instrument::getDescription);
        instrument.setRequired(true);
        right.add(instrument);

        var stimme = new ComboBox<Stimme>("Stimme");
        stimme.setItems(Stimme.values());
        stimme.setClearButtonVisible(true);
        stimme.setItemLabelGenerator(Stimme::getDescription);
        right.add(stimme);

        var stimmlage = new ComboBox<Stimmlage>("Stimmlage");
        stimmlage.setClearButtonVisible(true);
        stimmlage.setItems(Stimmlage.values());
        stimmlage.setItemLabelGenerator(Stimmlage::getDescription);
        right.add(stimmlage);

        save = primaryButton("Speichern", () -> {
            try {
                notenService.insert(komposition.id(),
                                    new NotenDto(null,
                                                 instrument.getValue(),
                                                 stimme.getOptionalValue().orElse(null),
                                                 stimmlage.getOptionalValue().orElse(null)),
                                    buffer.getInputStream().readAllBytes());

                upload.clearFileList();
                instrument.clear();
                stimme.clear();
                stimmlage.clear();
                Notification.show("Noten gespeichert!");

                grid.setItems(notenService.findByKomposition(komposition.id()));
            } catch (IOException e) {
                save.setEnabled(true);
                Notification.show("Ein Fehler ist aufgetreten");
            }
        });
        save.setDisableOnClick(true);
        right.add(save);

        add(new HorizontalLayout(left, right));

        getFooter().add(new Button("Schliessen", e -> close()));
    }
}
