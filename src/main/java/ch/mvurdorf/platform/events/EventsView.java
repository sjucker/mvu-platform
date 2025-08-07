package ch.mvurdorf.platform.events;

import ch.mvurdorf.platform.security.AuthenticatedUser;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import jakarta.annotation.security.RolesAllowed;

import static ch.mvurdorf.platform.security.LoginService.EVENTS_GROUP;
import static ch.mvurdorf.platform.security.LoginService.NOTEN_GROUP;
import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static ch.mvurdorf.platform.ui.RendererUtil.dateRenderer;
import static ch.mvurdorf.platform.ui.RendererUtil.dateTimeRenderer;
import static ch.mvurdorf.platform.ui.RendererUtil.timeRenderer;
import static com.vaadin.flow.component.grid.ColumnTextAlign.CENTER;
import static com.vaadin.flow.component.icon.VaadinIcon.COPY;
import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.data.value.ValueChangeMode.TIMEOUT;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.CALENDAR;

@PageTitle("Events")
@Route("events")
@RolesAllowed({EVENTS_GROUP})
@Menu(order = 6, icon = CALENDAR)
public class EventsView extends VerticalLayout {

    private final EventsService eventsService;
    private final AuthenticatedUser authenticatedUser;

    private HorizontalLayout controls;
    private Grid<EventDto> grid;
    private ConfigurableFilterDataProvider<EventDto, Void, String> dataProvider;

    public EventsView(EventsService eventsService, AuthenticatedUser authenticatedUser) {
        this.eventsService = eventsService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        createControls();
        createGrid();
        add(controls, grid);
    }

    private void createGrid() {
        grid = new Grid<>();
        dataProvider = eventsService.dataProvider();
        grid.setDataProvider(dataProvider);

        grid.addColumn(clickableIcon(EDIT, this::edit, "Bearbeiten")).setWidth("60px").setTextAlign(CENTER).setFlexGrow(0);
        grid.addColumn(clickableIcon(TRASH, this::delete, "Löschen")).setWidth("60px").setTextAlign(CENTER).setFlexGrow(0);
        grid.addColumn(clickableIcon(COPY, this::copy, "Kopieren")).setWidth("60px").setTextAlign(CENTER).setFlexGrow(0);
        grid.addColumn(EventDto::title).setHeader("Titel").setFlexGrow(1).setResizable(true);
        grid.addColumn(dateRenderer(EventDto::fromDate)).setHeader("Datum von").setWidth("100px").setFlexGrow(0);
        grid.addColumn(timeRenderer(EventDto::fromTime)).setHeader("Zeit von").setWidth("80px").setFlexGrow(0);
        grid.addColumn(dateRenderer(EventDto::toDate)).setHeader("Datum bis").setWidth("100px").setFlexGrow(0);
        grid.addColumn(timeRenderer(EventDto::toTime)).setHeader("Zeit bis").setWidth("80px").setFlexGrow(0);
        grid.addColumn(EventDto::location).setHeader("Räumlichkeiten");
        grid.addColumn(dateTimeRenderer(EventDto::createdAt)).setHeader("Zuletzt aktualisiert");
        grid.addColumn(EventDto::createdBy).setHeader("Bearbeitet durch");

        grid.addItemDoubleClickListener(event -> edit(event.getItem()));
    }

    private void createControls() {
        controls = new HorizontalLayout();
        controls.setWidthFull();
        controls.addClassNames(Display.FLEX, FlexDirection.COLUMN, JustifyContent.BETWEEN, FlexDirection.Breakpoint.Medium.ROW, Gap.SMALL);
        if (authenticatedUser.hasWritePermission(NOTEN_GROUP)) {
            controls.add(new Button("Event hinzufügen", _ -> EventDialog.show(newEvent -> {
                eventsService.insert(newEvent, authenticatedUser.getName());
                dataProvider.refreshAll();
            })));
        }

        var filter = new TextField(event -> dataProvider.setFilter(event.getValue()));
        filter.setPlaceholder("Filter");
        filter.setValueChangeMode(TIMEOUT);
        filter.setClearButtonVisible(true);
        controls.add(filter);
    }

    private void edit(EventDto item) {
        EventDialog.show(EventDataDto.of(item), event -> {
            eventsService.update(event, authenticatedUser.getName());
            dataProvider.refreshAll();
        });
    }

    private void copy(EventDto item) {
        EventDialog.show(EventDataDto.copy(item), newEvent -> {
            eventsService.insert(newEvent, authenticatedUser.getName());
            dataProvider.refreshAll();
        });
    }

    private void delete(EventDto item) {
        new ConfirmDialog("Event löschen?", "Soll der Event gelöscht werden? Permanent heisst, dass es nicht nachvollziehbar ist im Probeplan.",
                          "Permanent löschen",
                          _ -> {
                              eventsService.delete(item, true);
                              dataProvider.refreshAll();
                          },
                          "Nachvollziehbar löschen",
                          _ -> {
                              eventsService.delete(item, false);
                              dataProvider.refreshAll();
                          },
                          "Abbrechen", _ -> {}).open();
    }
}
