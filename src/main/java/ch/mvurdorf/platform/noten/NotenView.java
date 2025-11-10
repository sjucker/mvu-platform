package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.home.NotenDownloadDialog;
import ch.mvurdorf.platform.security.AuthenticatedUser;
import ch.mvurdorf.platform.service.StorageService;
import ch.mvurdorf.platform.ui.LocalizedEnumRenderer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Set;

import static ch.mvurdorf.platform.security.LoginService.NOTEN_GROUP;
import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static ch.mvurdorf.platform.ui.RendererUtil.externalLink;
import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.FILE_SOUND;
import static com.vaadin.flow.component.icon.VaadinIcon.MUSIC;
import static com.vaadin.flow.component.icon.VaadinIcon.UPLOAD;
import static com.vaadin.flow.data.value.ValueChangeMode.TIMEOUT;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.MUSIC_SOLID;

@PageTitle("Noten")
@Route("noten")
@RolesAllowed({NOTEN_GROUP})
@Menu(order = 5, icon = MUSIC_SOLID)
public class NotenView extends VerticalLayout {

    private final NotenService notenService;
    private final StorageService storageService;
    private final KompositionService kompositionService;
    private final NotenPdfUploadService notenPdfUploadService;
    private final AuthenticatedUser authenticatedUser;

    private HorizontalLayout controls;
    private Grid<KompositionDto> grid;
    private ConfigurableFilterDataProvider<KompositionDto, Void, String> dataProvider;

    public NotenView(NotenService notenService, StorageService storageService, KompositionService kompositionService, NotenPdfUploadService notenPdfUploadService, AuthenticatedUser authenticatedUser) {
        this.notenService = notenService;
        this.storageService = storageService;
        this.kompositionService = kompositionService;
        this.notenPdfUploadService = notenPdfUploadService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        createControls();
        createGrid();
        add(controls, grid);
    }

    private void createGrid() {
        grid = new Grid<>();
        dataProvider = kompositionService.dataProvider();
        grid.setDataProvider(dataProvider);

        grid.addColumn(clickableIcon(UPLOAD, dto -> NotenPdfUploadDialog.show(notenPdfUploadService, dto), "Noten-Upload")).setWidth("60px").setFlexGrow(0);
        grid.addColumn(clickableIcon(MUSIC, dto -> NotenDownloadDialog.show(notenService, storageService, Set.of(), dto.id(), dto.titel()), "Noten-Download")).setWidth("60px").setFlexGrow(0);
        grid.addColumn(clickableIcon(EDIT, this::edit, "Bearbeiten")).setWidth("60px").setFlexGrow(0);
        grid.addColumn(KompositionDto::titel).setHeader("Titel");
        grid.addColumn(KompositionDto::komponist).setHeader("Komponist");
        grid.addColumn(KompositionDto::arrangeur).setHeader("Arrangeur");
        grid.addColumn(new LocalizedEnumRenderer<>(KompositionDto::format)).setHeader("Format");
        grid.addColumn(externalLink(FILE_SOUND, KompositionDto::audioSample, "Hörprobe")).setHeader("Hörprobe");
        grid.addItemDoubleClickListener(event -> edit(event.getItem()));
    }

    private void edit(KompositionDto item) {
        KompositionDialog.show(item, kompositionDto -> {
            kompositionService.update(kompositionDto);
            dataProvider.refreshAll();
        });
    }

    private void createControls() {
        controls = new HorizontalLayout();
        if (authenticatedUser.hasWritePermission(NOTEN_GROUP)) {
            controls.add(new Button("Komposition hinzufügen", _ -> KompositionDialog.show(kompositionDto -> {
                kompositionService.insert(kompositionDto);
                dataProvider.refreshAll();
            })));
        }

        var filter = new TextField(event -> dataProvider.setFilter(event.getValue()));
        filter.setPlaceholder("Filter");
        filter.setValueChangeMode(TIMEOUT);
        filter.setClearButtonVisible(true);
        controls.add(filter);
    }
}
