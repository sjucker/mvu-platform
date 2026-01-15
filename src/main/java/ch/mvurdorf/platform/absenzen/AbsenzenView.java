package ch.mvurdorf.platform.absenzen;

import ch.mvurdorf.platform.security.AuthenticatedUser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadResponse;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static ch.mvurdorf.platform.security.LoginService.ABSENZEN_GROUP;
import static ch.mvurdorf.platform.ui.ComponentUtil.datePicker;
import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static ch.mvurdorf.platform.ui.RendererUtil.dateRenderer;
import static ch.mvurdorf.platform.utils.FormatUtil.formatDate;
import static com.vaadin.flow.component.grid.ColumnTextAlign.CENTER;
import static com.vaadin.flow.component.icon.VaadinIcon.CHECK_SQUARE_O;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.BASELINE;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN;
import static com.vaadin.flow.data.value.ValueChangeMode.TIMEOUT;
import static com.vaadin.flow.server.streams.DownloadHandler.fromInputStream;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.CLIPBOARD_CHECK_SOLID;

@Slf4j
@PageTitle("Absenzen")
@Route("absenzen")
@RolesAllowed({ABSENZEN_GROUP})
@Menu(order = 2, icon = CLIPBOARD_CHECK_SOLID)
public class AbsenzenView extends VerticalLayout {

    private final AbsenzenService absenzenService;
    private final AbsenzenExportService absenzenExportService;
    private final AuthenticatedUser authenticatedUser;

    private HorizontalLayout controls;
    private Grid<EventAbsenzSummaryDto> grid;
    private ConfigurableFilterDataProvider<EventAbsenzSummaryDto, Void, String> dataProvider;
    private DatePicker fromDate;
    private DatePicker toDate;
    private Button exportButton;

    public AbsenzenView(AbsenzenService absenzenService,
                        AbsenzenExportService absenzenExportService,
                        AuthenticatedUser authenticatedUser) {
        this.absenzenService = absenzenService;
        this.absenzenExportService = absenzenExportService;
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

        fromDate = datePicker("Von");
        toDate = datePicker("Bis");
        exportButton = new Button("Export");
        exportButton.setEnabled(false);
        exportButton.addClickListener(_ -> {
            var from = fromDate.getValue();
            var to = toDate.getValue();
            try (var inputStream = absenzenExportService.export(from, to)) {
                var resource = fromInputStream(_ -> new DownloadResponse(inputStream, "absenzen-%s-%s.xlsx".formatted(formatDate(from), formatDate(to)), "application/xlsx", -1));
                var registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
                UI.getCurrent().getPage().open(registration.getResourceUri().toString());
            } catch (IOException e) {
                log.error("could not export excel", e);
                Notification.show("Ein Fehler ist aufgetreten.");
            }
        });

        fromDate.addValueChangeListener(_ -> validateForm());
        toDate.addValueChangeListener(_ -> validateForm());

        var exportControls = new HorizontalLayout(fromDate, toDate, exportButton);
        exportControls.setAlignItems(BASELINE);

        controls.add(filter, exportControls);
        controls.setJustifyContentMode(BETWEEN);
        controls.setAlignItems(BASELINE);
    }

    private void validateForm() {
        var from = fromDate.getValue();
        var to = toDate.getValue();
        exportButton.setEnabled(from != null && to != null && !from.isAfter(to));
    }

    private void detail(EventAbsenzSummaryDto item) {
        AbsenzStatusDialog.show(absenzenService, authenticatedUser, item, () -> dataProvider.refreshAll());
    }
}
