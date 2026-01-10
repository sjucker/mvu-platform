package ch.mvurdorf.platform.users;

import ch.mvurdorf.platform.security.AuthenticatedUser;
import ch.mvurdorf.platform.ui.LocalizedEnumRenderer;
import com.google.firebase.auth.FirebaseAuthException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import static ch.mvurdorf.platform.security.LoginService.USERS_GROUP;
import static ch.mvurdorf.platform.ui.RendererUtil.checkboxRenderer;
import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static com.vaadin.flow.component.grid.ColumnTextAlign.CENTER;
import static com.vaadin.flow.component.grid.GridVariant.LUMO_COLUMN_BORDERS;
import static com.vaadin.flow.component.grid.GridVariant.LUMO_COMPACT;
import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.USER;

@Slf4j
@PageTitle("Users")
@Route("users")
@RolesAllowed({USERS_GROUP})
@Menu(order = 6, icon = USER)
class UsersView extends VerticalLayout {

    private final UsersService usersService;
    private final AuthenticatedUser authenticatedUser;

    private HorizontalLayout controls;
    private Grid<UserDto> grid;
    private GridListDataView<UserDto> dataView;
    private Checkbox activeUsersOnly;

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
        controls.setAlignItems(Alignment.CENTER);
        controls.setJustifyContentMode(JustifyContentMode.BETWEEN);
        controls.setWidthFull();

        if (authenticatedUser.hasWritePermission(USERS_GROUP)) {
            controls.add(new Button("Neuer User",
                                    _ -> UserDialog.create(newUser -> usersService.create(newUser)
                                                                                  .ifPresentOrElse(
                                                                                          password -> {
                                                                                              Notification.show("User erstellt mit Passwort: " + password, 0, TOP_CENTER);
                                                                                              setGridItems();
                                                                                          },
                                                                                          () -> Notification.show("User konnte nicht erstellt werden.")))));
        }

        activeUsersOnly = new Checkbox("nur Aktive");
        activeUsersOnly.setValue(true);
        activeUsersOnly.addValueChangeListener(_ -> dataView.setFilter(dataFilter()));
        controls.add(activeUsersOnly);
    }

    private void createGrid() {
        grid = new Grid<>();
        grid.addThemeVariants(LUMO_COLUMN_BORDERS, LUMO_COMPACT);
        grid.setHeightFull();

        grid.addColumn(clickableIcon(EDIT, this::edit)).setWidth("60px").setTextAlign(CENTER).setFlexGrow(0);
        grid.addColumn(UserDto::email).setHeader("Email");
        grid.addColumn(UserDto::name).setHeader("Name");
        grid.addColumn(new LocalizedEnumRenderer<>(UserDto::register)).setHeader("Register");
        grid.addColumn(checkboxRenderer(UserDto::sendReminder)).setHeader("Reminder").setWidth("80px").setTextAlign(CENTER).setFlexGrow(0);
        grid.addColumn(checkboxRenderer(UserDto::active)).setHeader("Aktiv").setWidth("60px").setTextAlign(CENTER).setFlexGrow(0);

        grid.addItemDoubleClickListener(event -> edit(event.getItem()));

        setGridItems();
    }

    private void setGridItems() {
        dataView = grid.setItems(usersService.findAll());
        dataView.setFilter(dataFilter());
    }

    private void edit(UserDto item) {
        UserDialog.edit(item, user -> {
            try {
                usersService.update(user);
                setGridItems();
            } catch (FirebaseAuthException e) {
                Notification.show("Fehler beim Aktualisieren des Users: " + e.getMessage(), 0, TOP_CENTER);
                log.error("could not update user %s".formatted(user), e);
            }
        });
    }

    private SerializablePredicate<UserDto> dataFilter() {
        return dto -> !activeUsersOnly.getValue() || dto.active();
    }
}
