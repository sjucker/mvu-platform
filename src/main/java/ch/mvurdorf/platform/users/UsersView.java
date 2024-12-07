package ch.mvurdorf.platform.users;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import static com.vaadin.flow.component.grid.Grid.SelectionMode.MULTI;
import static com.vaadin.flow.component.grid.GridVariant.LUMO_COLUMN_BORDERS;
import static com.vaadin.flow.component.grid.GridVariant.LUMO_COMPACT;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.USER;

@PageTitle("Users")
@Route("users")
@PermitAll
@Menu(order = 1, icon = USER)
class UsersView extends VerticalLayout {

    private final UsersService usersService;

    private Grid<UserDto> grid;
    private GridListDataView<UserDto> gridListDataView;

    public UsersView(UsersService usersService) {
        this.usersService = usersService;

        setSizeFull();
        createContent();
        add(grid);
    }

    private void createContent() {
        grid = new Grid<>();
        grid.setSelectionMode(MULTI);
        grid.addThemeVariants(LUMO_COLUMN_BORDERS, LUMO_COMPACT);
        grid.setHeightFull();

        gridListDataView = grid.setItems(usersService.findAll());

        grid.addColumn(UserDto::name).setHeader("Name");
    }
}
