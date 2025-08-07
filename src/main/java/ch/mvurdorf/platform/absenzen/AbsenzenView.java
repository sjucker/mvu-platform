package ch.mvurdorf.platform.absenzen;

import ch.mvurdorf.platform.events.EventDto;
import ch.mvurdorf.platform.events.EventsService;
import ch.mvurdorf.platform.security.AuthenticatedUser;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import static ch.mvurdorf.platform.security.LoginService.ABSENZEN_GROUP;
import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static ch.mvurdorf.platform.ui.RendererUtil.dateRenderer;
import static com.vaadin.flow.component.grid.ColumnTextAlign.CENTER;
import static com.vaadin.flow.component.icon.VaadinIcon.CHECK_SQUARE_O;
import static com.vaadin.flow.data.value.ValueChangeMode.TIMEOUT;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.CLIPBOARD_CHECK_SOLID;

@PageTitle("Absenzen")
@Route("absenzen")
@RolesAllowed({ABSENZEN_GROUP})
@Menu(order = 7, icon = CLIPBOARD_CHECK_SOLID)
public class AbsenzenView extends VerticalLayout {

    private final EventsService eventsService;
    private final AbsenzenService absenzenService;
    private final AuthenticatedUser authenticatedUser;

    private HorizontalLayout controls;
    private Grid<EventDto> grid;
    private ConfigurableFilterDataProvider<EventDto, Void, String> dataProvider;

    public AbsenzenView(EventsService eventsService, AbsenzenService absenzenService, AuthenticatedUser authenticatedUser) {
        this.eventsService = eventsService;
        this.absenzenService = absenzenService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        createControls();
        createGrid();
        add(controls, grid);
    }

    private void createGrid() {
        grid = new Grid<>();
        dataProvider = eventsService.dataProviderAbsenzen();
        grid.setDataProvider(dataProvider);

        grid.addColumn(clickableIcon(CHECK_SQUARE_O, this::detail, "Details")).setWidth("60px").setTextAlign(CENTER).setFlexGrow(0);
        grid.addColumn(dateRenderer(EventDto::fromDate)).setWidth("150px").setFlexGrow(0);
        grid.addColumn(EventDto::title).setFlexGrow(1).setResizable(true);

        grid.addItemDoubleClickListener(event -> detail(event.getItem()));
    }

    private void createControls() {
        controls = new HorizontalLayout();
        controls.setWidthFull();

        var filter = new TextField(event -> dataProvider.setFilter(event.getValue()));
        filter.setPlaceholder("Filter");
        filter.setValueChangeMode(TIMEOUT);
        filter.setClearButtonVisible(true);

        controls.add(filter);
    }

    private void detail(EventDto item) {
        AbsenzStatusDialog.show(absenzenService, item.id());
    }
}
