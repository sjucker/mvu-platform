package ch.mvurdorf.platform.passivmitglied;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import static ch.mvurdorf.platform.security.LoginService.PASSIVMITGLIED_GROUP;
import static com.vaadin.flow.data.value.ValueChangeMode.TIMEOUT;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.MONEY_BILL_ALT;

@PageTitle("Passivmitglieder")
@Route("passivmitglied")
@RolesAllowed({PASSIVMITGLIED_GROUP})
@Menu(order = 3, icon = MONEY_BILL_ALT)
public class PassivmitgliedView extends VerticalLayout {

    private final PassivmitgliedService passivmitgliedService;

    private HorizontalLayout controls;
    private Grid<PassivmitgliedDto> grid;
    private ConfigurableFilterDataProvider<PassivmitgliedDto, Void, String> dataProvider;

    public PassivmitgliedView(PassivmitgliedService passivmitgliedService) {
        this.passivmitgliedService = passivmitgliedService;

        setSizeFull();
        createControls();
        createGrid();
        add(controls, grid);
    }

    private void createControls() {
        controls = new HorizontalLayout();
        var filter = new TextField(event -> dataProvider.setFilter(event.getValue()));
        filter.setPlaceholder("Filter");
        filter.setValueChangeMode(TIMEOUT);
        filter.setClearButtonVisible(true);
        controls.add(filter);
    }

    private void createGrid() {
        grid = new Grid<>();

        grid.addColumn(PassivmitgliedDto::vorname).setHeader("Vorname");
        grid.addColumn(PassivmitgliedDto::nachname).setHeader("Nachname");
        grid.addColumn(PassivmitgliedDto::email).setHeader("Email");
        grid.addColumn(PassivmitgliedDto::strasse).setHeader("Strasse");
        grid.addColumn(PassivmitgliedDto::ort).setHeader("PLZ/Ort");
        grid.addColumn(new LocalDateTimeRenderer<>(PassivmitgliedDto::registeredAt, "dd.MM.yyyy hh:mm")).setHeader("Registriert am");
        grid.addColumn(PassivmitgliedDto::numberOfPayments).setHeader("# Bezahlungen");
        grid.addColumn(new LocalDateRenderer<>(dto -> dto.lastPayment().orElse(null), "dd.MM.yyyy")).setHeader("Letzte Bezahlung");

        dataProvider = passivmitgliedService.dataProvider();
        grid.setDataProvider(dataProvider);
    }
}
