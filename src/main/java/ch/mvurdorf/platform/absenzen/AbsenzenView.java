package ch.mvurdorf.platform.absenzen;

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

    private final AbsenzenService absenzenService;
    private final AuthenticatedUser authenticatedUser;

    private HorizontalLayout controls;
    private Grid<EventAbsenzSummaryDto> grid;
    private ConfigurableFilterDataProvider<EventAbsenzSummaryDto, Void, String> dataProvider;

    public AbsenzenView(AbsenzenService absenzenService, AuthenticatedUser authenticatedUser) {
        this.absenzenService = absenzenService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        createControls();
        createGrid();
        add(controls, grid);
    }

    private void createGrid() {
        grid = new Grid<>();
        dataProvider = absenzenService.dataProvider();
        grid.setDataProvider(dataProvider);

        grid.addColumn(clickableIcon(CHECK_SQUARE_O, this::detail, "Details")).setWidth("60px").setTextAlign(CENTER).setFlexGrow(0);
        grid.addColumn(dateRenderer(EventAbsenzSummaryDto::fromDate)).setWidth("150px").setFlexGrow(0);
        grid.addColumn(EventAbsenzSummaryDto::title).setWidth("300px").setFlexGrow(0).setResizable(true);
        grid.addColumn(EventAbsenzSummaryDto::totalPositive).setHeader("Total anwesend").setWidth("140px").setFlexGrow(0);
        grid.addColumn(EventAbsenzSummaryDto::totalNegative).setHeader("Total abwesend").setWidth("140px");

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

    private void detail(EventAbsenzSummaryDto item) {
        AbsenzStatusDialog.show(absenzenService, authenticatedUser, item, () -> dataProvider.refreshAll());
    }
}
