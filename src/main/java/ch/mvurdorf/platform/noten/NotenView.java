package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.security.AuthenticatedUser;
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

import static ch.mvurdorf.platform.security.LoginService.NOTEN_GROUP;
import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static com.vaadin.flow.component.icon.VaadinIcon.UPLOAD;
import static com.vaadin.flow.data.value.ValueChangeMode.TIMEOUT;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.MUSIC_SOLID;

@PageTitle("Noten")
@Route("noten")
@RolesAllowed({NOTEN_GROUP})
@Menu(order = 2, icon = MUSIC_SOLID)
public class NotenView extends VerticalLayout {

    private final NotenService notenService;
    private final KompositionService kompositionService;
    private final AuthenticatedUser authenticatedUser;

    private HorizontalLayout controls;
    private Grid<KompositionDto> grid;
    private ConfigurableFilterDataProvider<KompositionDto, Void, String> dataProvider;

    public NotenView(NotenService notenService, KompositionService kompositionService, AuthenticatedUser authenticatedUser) {
        this.notenService = notenService;
        this.kompositionService = kompositionService;
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

        grid.addColumn(KompositionDto::titel).setHeader("Titel");
        grid.addColumn(KompositionDto::komponist).setHeader("Komponist");
        grid.addColumn(KompositionDto::arrangeur).setHeader("Arrangeur");
        grid.addColumn(clickableIcon(UPLOAD, dto -> NotenDialog.show(notenService, dto)));
    }

    private void createControls() {
        controls = new HorizontalLayout();
        if (authenticatedUser.hasWritePermission(NOTEN_GROUP)) {
            controls.add(new Button("Komposition hinzufügen", event -> {
                KompositionDialog.show(kompositionDto -> {
                    kompositionService.insert(kompositionDto);
                    dataProvider.refreshAll();
                });
            }));
        }

        var filter = new TextField(event -> dataProvider.setFilter(event.getValue()));
        filter.setPlaceholder("Filter");
        filter.setValueChangeMode(TIMEOUT);
        filter.setClearButtonVisible(true);
        controls.add(filter);
    }
}
