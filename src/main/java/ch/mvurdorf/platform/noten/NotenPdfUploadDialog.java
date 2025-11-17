package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Notenschluessel;
import ch.mvurdorf.platform.common.Stimme;
import ch.mvurdorf.platform.common.Stimmlage;
import ch.mvurdorf.platform.ui.LocalizedEnumRenderer;
import ch.mvurdorf.platform.ui.i18n.UploadGermanI18N;
import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.ScrollOptions;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.Loader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static com.vaadin.flow.component.ScrollOptions.Behavior.SMOOTH;
import static com.vaadin.flow.component.Unit.PIXELS;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.math.NumberUtils.toInt;

@Slf4j
@RequiredArgsConstructor(access = PRIVATE)
public class NotenPdfUploadDialog extends Dialog {

    private final NotenPdfUploadService notenPdfUploadService;

    private boolean pdfUploaded = false;
    private int pageCount = 0;
    private PdfViewer pdfViewer;
    private Scroller scroller;
    private NotenPdfAssignmentContainer notenPdfAssignmentContainer;
    private Button save;
    private byte[] uploadedFile;

    public static void show(NotenPdfUploadService notenPdfUploadService, KompositionDto komposition) {
        var dialog = new NotenPdfUploadDialog(notenPdfUploadService);
        dialog.init(komposition);
        dialog.setModal(true);
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setSizeFull();
        dialog.open();
    }

    private void init(KompositionDto komposition) {
        setHeaderTitle(komposition.titel());

        var upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            pdfUploaded = true;
            uploadedFile = data;
            save.setEnabled(true);
            scroller.setVisible(true);
            pdfViewer.setSrc(DownloadHandler.fromInputStream(_ -> new DownloadResponse(new ByteArrayInputStream(data),
                                                                                       metadata.fileName(), metadata.contentType(), metadata.contentLength())));
            pdfViewer.setCustomTitle(metadata.fileName());
            pageCount = getPdfPageCount();
            notenPdfAssignmentContainer.add("%d-%d".formatted(1, pageCount));
        }));
        upload.setI18n(new UploadGermanI18N());
        upload.setAcceptedFileTypes(".pdf");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(1024 * 1024 * 100);
        upload.addFileRemovedListener(_ -> {
            pdfUploaded = false;
            save.setEnabled(false);
            scroller.setVisible(false);
            pdfViewer.setSrc("");
            pdfViewer.setCustomTitle(null);
            notenPdfAssignmentContainer.reset();
        });
        upload.setWidthFull();

        notenPdfAssignmentContainer = new NotenPdfAssignmentContainer();
        scroller = new Scroller(notenPdfAssignmentContainer);
        scroller.setSizeFull();
        scroller.setVisible(false);

        var content = new VerticalLayout(upload, scroller);
        content.setPadding(false);

        pdfViewer = new PdfViewer();
        pdfViewer.setSizeFull();
        pdfViewer.setAddDownloadButton(false);

        var splitLayout = new SplitLayout(content, pdfViewer);
        splitLayout.setSizeFull();
        add(splitLayout);

        getFooter().add(new Button("Verwerfen", _ -> close()));
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
                                                 uploadedFile);
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

    private int getPdfPageCount() {
        try (var pdDocument = Loader.loadPDF(uploadedFile)) {
            return pdDocument.getNumberOfPages();
        } catch (IOException _) {
            return 0;
        }
    }

    @Override
    public void close() {
        // make sure memory is released
        uploadedFile = null;
        super.close();
    }

    private static class NotenPdfAssignmentContainer extends Composite<VerticalLayout> {

        private final List<NotenPdfAssignment> assignments = new ArrayList<>();

        public NotenPdfAssignmentContainer() {
            getContent().setSizeFull();
            getContent().setMargin(false);
            getContent().setPadding(false);

            var add = new Button(PLUS.create(), _ -> {
                var pages = assignments.getLast().getPages();
                var nextPages = pages.isValid() ? "%d-%d".formatted(pages.to + 1, pages.to + 1 + (pages.to - pages.from)) : "";
                var notenPdfAssignment = new NotenPdfAssignment(nextPages);
                assignments.add(notenPdfAssignment);
                getContent().addComponentAtIndex(assignments.size() - 1, notenPdfAssignment);
                notenPdfAssignment.scrollIntoView(new ScrollOptions(SMOOTH));
            });
            getContent().add(add);
        }

        public List<CreateNotenPdfDto> get() {
            return assignments.stream()
                              .flatMap(assignment -> assignment.get().stream())
                              .toList();
        }

        public void add(String pages) {
            var notenPdfAssignment = new NotenPdfAssignment(pages);
            assignments.add(notenPdfAssignment);
            getContent().addComponentAtIndex(assignments.size() - 1, notenPdfAssignment);
        }

        public void reset() {
            getContent().removeAll();
            assignments.clear();
        }
    }

    private static class NotenPdfAssignment extends Composite<HorizontalLayout> {

        private final Select<Notenschluessel> notenschluessel;
        private final Select<Stimmlage> stimmlage;
        private final MultiSelectComboBox<Instrument> instrumente;
        private final MultiSelectComboBox<Stimme> stimmen;
        private final TextField pages;

        public NotenPdfAssignment(String pagesValue) {
            notenschluessel = new Select<>();
            notenschluessel.setEmptySelectionAllowed(true);
            notenschluessel.setLabel("Notenschlüssel");
            notenschluessel.setItems(Notenschluessel.values());
            notenschluessel.setRenderer(new LocalizedEnumRenderer<>(s -> s));
            notenschluessel.setWidth(150, PIXELS);

            stimmlage = new Select<>();
            stimmlage.setEmptySelectionAllowed(true);
            stimmlage.setLabel("Stimmlage");
            stimmlage.setItems(Stimmlage.values());
            stimmlage.setRenderer(new LocalizedEnumRenderer<>(s -> s));
            stimmlage.setWidth(100, PIXELS);

            instrumente = new MultiSelectComboBox<>("Instrumente");
            instrumente.setItems(Instrument.values());
            instrumente.setItemLabelGenerator(Instrument::getDescription);
            instrumente.setRequired(true);
            instrumente.setWidth(240, PIXELS);

            stimmen = new MultiSelectComboBox<>("Stimmen");
            stimmen.setItems(Stimme.values());
            stimmen.setItemLabelGenerator(Stimme::getDescription);
            stimmen.setWidth(140, PIXELS);

            pages = new TextField("Seiten");
            pages.setValue(pagesValue);
            pages.setPlaceholder("z.B. 1-5 oder 9");
            pages.setRequired(true);
            pages.setWidth(100, PIXELS);

            getContent().setWidthFull();
            getContent().setMargin(false);
            getContent().setPadding(false);
            getContent().setWrap(true);
            getContent().addClassNames(Gap.Row.XSMALL, Gap.Column.MEDIUM);
            getContent().add(notenschluessel, stimmlage, instrumente, stimmen, pages);
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
                                            notenschluessel.getOptionalValue().orElse(null),
                                            assignments,
                                            pagesFromTo.from(), pagesFromTo.to());

            return Optional.of(dto);
        }

        public Pages getPages() {
            return parsePages(pages.getValue());
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
