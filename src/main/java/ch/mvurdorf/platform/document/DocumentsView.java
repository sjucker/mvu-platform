package ch.mvurdorf.platform.document;

import ch.mvurdorf.platform.security.AuthenticatedUser;
import ch.mvurdorf.platform.service.StorageService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ch.mvurdorf.platform.security.LoginService.DOCUMENT_GROUP;
import static ch.mvurdorf.platform.security.LoginService.USERS_GROUP;
import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static com.vaadin.flow.component.icon.VaadinIcon.DOWNLOAD;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.data.value.ValueChangeMode.TIMEOUT;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.FILE_PDF_SOLID;

@Slf4j
@PageTitle("Dokumente")
@Route("documents")
@RolesAllowed({DOCUMENT_GROUP})
@Menu(order = 10, icon = FILE_PDF_SOLID)
public class DocumentsView extends VerticalLayout {

    private final DocumentService documentService;
    private final StorageService storageService;
    private final AuthenticatedUser authenticatedUser;

    private HorizontalLayout controls;
    private Grid<DocumentDto> grid;
    private List<DocumentDto> documents;
    private TextField filterField;

    public DocumentsView(DocumentService documentService, StorageService storageService, AuthenticatedUser authenticatedUser) {
        this.documentService = documentService;
        this.storageService = storageService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        createControls();
        createGrid();
        add(controls, grid);

        refreshGrid();
    }

    private void createGrid() {
        grid = new Grid<>();

        grid.addColumn(DocumentDto::name).setHeader("Name").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(DocumentDto::fileType).setHeader("Type").setAutoWidth(true);
        grid.addColumn(doc -> doc.uploadDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
            .setHeader("Upload Date").setAutoWidth(true);
        grid.addColumn(DocumentDto::description).setHeader("Description").setAutoWidth(true).setFlexGrow(1);

        grid.addColumn(doc -> {
            StreamResource resource = new StreamResource(
                doc.name(),
                () -> {
                    var content = documentService.getDocumentContent(doc.id()).orElse(new byte[0]);
                    return new ByteArrayInputStream(content);
                }
            );

            Anchor anchor = new Anchor(resource, "");
            anchor.getElement().setAttribute("download", true);
            anchor.add(DOWNLOAD.create());
            return anchor;
        }).setHeader("Download").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(clickableIcon(TRASH, this::deleteDocument, "Delete"))
            .setWidth("60px").setFlexGrow(0);
    }

    private void deleteDocument(DocumentDto document) {
        documentService.deleteDocument(document.id());
        refreshGrid();
    }

    private void createControls() {
        controls = new HorizontalLayout();
        controls.setWidthFull();

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("application/pdf", ".pdf");
        upload.setMaxFiles(1);

        TextField nameField = new TextField("Document Name");
        nameField.setRequired(true);

        TextField descriptionField = new TextField("Description");

        upload.addSucceededListener(event -> {
            try {
                String fileName = event.getFileName();
                if (nameField.getValue().isEmpty()) {
                    nameField.setValue(fileName);
                }

                CreateDocumentDto documentDto = new CreateDocumentDto(
                    nameField.getValue(),
                    event.getMIMEType(),
                    descriptionField.getValue()
                );

                documentService.uploadDocument(documentDto, buffer.getInputStream().readAllBytes());

                nameField.clear();
                descriptionField.clear();
                upload.clearFileList();

                refreshGrid();
            } catch (Exception e) {
                log.error("Error uploading document", e);
            }
        });

        HorizontalLayout uploadLayout = new HorizontalLayout(nameField, descriptionField, upload);
        uploadLayout.setAlignItems(Alignment.END);

        filterField = new TextField();
        filterField.setPlaceholder("Filter");
        filterField.setClearButtonVisible(true);
        filterField.setValueChangeMode(TIMEOUT);
        filterField.addValueChangeListener(e -> applyFilter(e.getValue()));

        controls.add(uploadLayout, filterField);
        controls.setFlexGrow(1, uploadLayout);
    }

    private void refreshGrid() {
        documents = documentService.getAllDocuments();
        applyFilter(filterField != null ? filterField.getValue() : "");
    }

    private void applyFilter(String filter) {
        if (filter == null || filter.isEmpty()) {
            grid.setItems(documents);
        } else {
            String lowerCaseFilter = filter.toLowerCase();
            grid.setItems(documents.stream()
                .filter(doc ->
                    doc.name().toLowerCase().contains(lowerCaseFilter) ||
                    (doc.description() != null && doc.description().toLowerCase().contains(lowerCaseFilter))
                )
                .toList());
        }
    }
}
