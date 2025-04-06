package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Stimme;
import ch.mvurdorf.platform.common.Stimmlage;
import ch.mvurdorf.platform.ui.i18n.UploadGermanI18N;
import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static com.vaadin.flow.component.Unit.PIXELS;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.math.NumberUtils.toInt;

@RequiredArgsConstructor(access = PRIVATE)
public class NotenPdfUploadDialog extends Dialog {

    private final NotenPdfUploadService notenPdfUploadService;

    private boolean pdfUploaded = false;
    private PdfViewer pdfViewer;
    private Scroller scroller;
    private NotenPdfAssignmentContainer notenPdfAssignmentContainer;
    private Button save;
    private MemoryBuffer buffer;
    private Upload upload;

    public static void show(NotenPdfUploadService notenPdfUploadService, KompositionDto komposition) {
        var dialog = new NotenPdfUploadDialog(notenPdfUploadService);
        dialog.init(komposition);
        dialog.setModal(true);
        dialog.setSizeFull();
        dialog.open();
    }

    private void init(KompositionDto komposition) {
        setHeaderTitle(komposition.titel());

        buffer = new MemoryBuffer();
        upload = new Upload(buffer);
        upload.setI18n(new UploadGermanI18N());
        upload.setAcceptedFileTypes(".pdf");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(1024 * 1024 * 100);
        upload.addSucceededListener(e -> {
            pdfUploaded = true;
            save.setEnabled(true);
            scroller.setVisible(true);
            pdfViewer.setSrc(new StreamResource(buffer.getFileName(), buffer::getInputStream));
        });
        upload.addFileRemovedListener(e -> {
            pdfUploaded = false;
            save.setEnabled(false);
            scroller.setVisible(false);
            pdfViewer.setSrc("");
        });
        upload.setWidthFull();

        notenPdfAssignmentContainer = new NotenPdfAssignmentContainer();
        scroller = new Scroller(notenPdfAssignmentContainer);
        scroller.setSizeFull();
        scroller.setVisible(false);

        var content = new VerticalLayout(upload, scroller);

        pdfViewer = new PdfViewer();
        pdfViewer.setSizeFull();

        var splitLayout = new SplitLayout(content, pdfViewer);
        splitLayout.setSizeFull();
        add(splitLayout);

        getFooter().add(new Button("Verwerfen", e -> close()));
        save = primaryButton("Speichern", () -> {
            if (!pdfUploaded) {
                Notification.show("Zuerst ein PDF hochladen!");
                save.setEnabled(true);
                return;
            }

            var assignments = notenPdfAssignmentContainer.get();
            if (assignments.isEmpty()) {
                Notification.show("Keine Notenzuweisungen vorhanden!");
                save.setEnabled(true);
            } else {
                try {
                    notenPdfUploadService.upload(komposition.id(),
                                                 assignments,
                                                 buffer.getInputStream().readAllBytes());
                    Notification.show("Noten erfolgreich hochgeladen!");
                    close();
                } catch (Exception e) {
                    save.setEnabled(true);
                    Notification.show("Fehler: " + e.getMessage());
                }
            }
        });
        save.setDisableOnClick(true);
        save.setEnabled(false);
        getFooter().add(save);
    }

    @Override
    public void close() {
        // make sure memory is released
        buffer = new MemoryBuffer();
        upload.setReceiver(buffer);
        super.close();
    }

    private static class NotenPdfAssignmentContainer extends Composite<VerticalLayout> {

        private final List<NotenPdfAssignment> assignments = new ArrayList<>();

        public NotenPdfAssignmentContainer() {
            getContent().setSizeFull();

            var add = new Button(VaadinIcon.PLUS.create(), e -> {
                var notenPdfAssignment = new NotenPdfAssignment();
                assignments.add(notenPdfAssignment);
                getContent().addComponentAsFirst(notenPdfAssignment);
            });
            getContent().add(add);
            add.click();
        }

        public List<CreateNotenPdfDto> get() {
            return assignments.stream()
                              .flatMap(assignment -> assignment.get().stream())
                              .toList();
        }
    }

    private static class NotenPdfAssignment extends Composite<HorizontalLayout> {

        private final Select<Stimmlage> stimmlage;
        private final MultiSelectComboBox<Instrument> instrumente;
        private final MultiSelectComboBox<Stimme> stimmen;
        private final TextField pages;

        public NotenPdfAssignment() {
            stimmlage = new Select<>();
            stimmlage.setLabel("Stimmlage");
            stimmlage.setItems(Stimmlage.values());
            stimmlage.setItemLabelGenerator(Stimmlage::getDescription);
            stimmlage.setWidthFull();

            instrumente = new MultiSelectComboBox<>("Instrumente");
            instrumente.setItems(Instrument.values());
            instrumente.setItemLabelGenerator(Instrument::getDescription);
            instrumente.setRequired(true);
            instrumente.setWidthFull();

            stimmen = new MultiSelectComboBox<>("Stimmen");
            stimmen.setItems(Stimme.values());
            stimmen.setItemLabelGenerator(Stimme::getDescription);
            stimmen.setWidthFull();

            pages = new TextField("Seiten");
            pages.setWidth(140, PIXELS);
            pages.setPlaceholder("z.B. 1-5 oder 9");
            pages.setRequired(true);

            getContent().setWidthFull();
            getContent().add(stimmlage, instrumente, stimmen, pages);
        }

        public Optional<CreateNotenPdfDto> get() {
            if (instrumente.isEmpty() || pages.isEmpty()) {
                return Optional.empty();
            }

            var pagesFromTo = parsePages(pages.getValue());
            if (!pagesFromTo.isValid()) {
                return Optional.empty();
            }

            List<CreateNotenPdfAssignmentDto> assignments;
            if (stimmen.isEmpty()) {
                assignments = instrumente.getValue().stream()
                                         .map(instrument -> new CreateNotenPdfAssignmentDto(instrument, null))
                                         .toList();
            } else {
                // make cross-product
                assignments = instrumente.getValue().stream()
                                         .flatMap(instrument -> stimmen.getValue().stream()
                                                                       .map(stimme -> new CreateNotenPdfAssignmentDto(instrument, stimme)))
                                         .toList();
            }

            var dto = new CreateNotenPdfDto(stimmlage.getOptionalValue().orElse(null),
                                            assignments,
                                            pagesFromTo.from(), pagesFromTo.to());

            return Optional.of(dto);
        }

    }

    protected static Pages parsePages(String pages) {
        if (StringUtils.isBlank(pages)) {
            return Pages.invalid();
        }

        var split = StringUtils.split(pages, '-');
        if (split.length == 2) {
            return new Pages(toInt(split[0]), toInt(split[1]));
        } else {
            return new Pages(toInt(split[0]), toInt(split[0]));
        }
    }

    protected record Pages(int from, int to) {

        public static Pages invalid() {
            return new Pages(0, 0);
        }

        public boolean isValid() {
            if (from == 0 || to == 0) {
                return false;
            }
            return from <= to;
        }
    }

}
