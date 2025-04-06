package ch.mvurdorf.platform.users;

import ch.mvurdorf.platform.security.AuthenticatedUser;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import static ch.mvurdorf.platform.security.LoginService.USERS_GROUP;
import static com.vaadin.flow.component.grid.GridVariant.LUMO_COLUMN_BORDERS;
import static com.vaadin.flow.component.grid.GridVariant.LUMO_COMPACT;
import static com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.USER;

@PageTitle("Users")
@Route("users")
@RolesAllowed({USERS_GROUP})
@Menu(order = 1, icon = USER)
class UsersView extends VerticalLayout {

    private final UsersService usersService;
    private final AuthenticatedUser authenticatedUser;

    private HorizontalLayout controls;
    private Grid<UserDto> grid;
    private GridListDataView<UserDto> gridListDataView;

    public UsersView(UsersService usersService, AuthenticatedUser authenticatedUser) {
        this.usersService = usersService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        createControls();
        createGrid();
        add(controls, grid);
    }

    private void createControls() {
        controls = new HorizontalLayout();
        if (authenticatedUser.hasWritePermission(USERS_GROUP)) {
            controls.add(new Button("Neuer User",
                                    _ -> UserDialog.create(newUser -> {
                                        var password = usersService.create(newUser);
                                        Notification.show("User erstellt mit Passwort: " + password, 0, TOP_CENTER);
                                        grid.setItems(usersService.findAll());
                                    })));
        }
    }

    private void createGrid() {
        grid = new Grid<>();
        grid.addThemeVariants(LUMO_COLUMN_BORDERS, LUMO_COMPACT);
        grid.setHeightFull();

        gridListDataView = grid.setItems(usersService.findAll());

        grid.addColumn(UserDto::email)
            .setHeader("Email");
        grid.addColumn(UserDto::name)
            .setHeader("Name");
        grid.addColumn(new ComponentRenderer<>(userDto -> userDto.active() ? VaadinIcon.CHECK.create() : new Div()))
            .setHeader("Aktiv");

        grid.addItemDoubleClickListener(event -> UserDialog.edit(event.getItem(), user -> {
            usersService.update(user);
            grid.setItems(usersService.findAll());
        }));
    }
}
