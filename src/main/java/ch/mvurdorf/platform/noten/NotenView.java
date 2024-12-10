package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.security.AuthenticatedUser;
import ch.mvurdorf.platform.users.UserDto;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import static ch.mvurdorf.platform.security.LoginService.NOTEN_GROUP;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.MUSIC_SOLID;

@PageTitle("Noten")
@Route("noten")
@RolesAllowed({NOTEN_GROUP})
@Menu(order = 2, icon = MUSIC_SOLID)
public class NotenView extends VerticalLayout {

    private final NotenService notenService;
    private final AuthenticatedUser authenticatedUser;

    private HorizontalLayout controls;
    private Grid<KompositionDto> grid;
    private GridListDataView<KompositionDto> gridListDataView;

    public NotenView(NotenService notenService, AuthenticatedUser authenticatedUser) {
        this.notenService = notenService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        createControls();
        createGrid();
        add(controls, grid);
    }

    private void createGrid() {
        controls = new HorizontalLayout();
    }

    private void createControls() {
        grid = new Grid<>();
    }
}
