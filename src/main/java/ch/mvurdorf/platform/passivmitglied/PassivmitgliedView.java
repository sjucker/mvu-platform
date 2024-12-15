package ch.mvurdorf.platform.passivmitglied;

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

import static ch.mvurdorf.platform.security.LoginService.PASSIVMITGLIED_GROUP;
import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static ch.mvurdorf.platform.ui.RendererUtil.dateRenderer;
import static ch.mvurdorf.platform.ui.RendererUtil.dateTimeRenderer;
import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.EYE;
import static com.vaadin.flow.data.value.ValueChangeMode.TIMEOUT;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.MONEY_BILL_ALT;

@PageTitle("Passivmitglieder")
@Route("passivmitglied")
@RolesAllowed({PASSIVMITGLIED_GROUP})
@Menu(order = 3, icon = MONEY_BILL_ALT)
public class PassivmitgliedView extends VerticalLayout {

    private final PassivmitgliedService passivmitgliedService;
    private final AuthenticatedUser authenticatedUser;

    private HorizontalLayout controls;
    private Grid<PassivmitgliedDto> grid;
    private ConfigurableFilterDataProvider<PassivmitgliedDto, Void, String> dataProvider;

    public PassivmitgliedView(PassivmitgliedService passivmitgliedService,
                              AuthenticatedUser authenticatedUser) {
        this.passivmitgliedService = passivmitgliedService;
        this.authenticatedUser = authenticatedUser;

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

        grid.addColumn(PassivmitgliedDto::externalId).setHeader("Referenz-Nr.");
        grid.addColumn(PassivmitgliedDto::vorname).setHeader("Vorname");
        grid.addColumn(PassivmitgliedDto::nachname).setHeader("Nachname");
        grid.addColumn(PassivmitgliedDto::email).setHeader("Email");
        grid.addColumn(PassivmitgliedDto::strasse).setHeader("Strasse");
        grid.addColumn(PassivmitgliedDto::ort).setHeader("PLZ/Ort");
        grid.addColumn(dateTimeRenderer(PassivmitgliedDto::registeredAt)).setHeader("Registriert am");
        grid.addColumn(PassivmitgliedDto::numberOfPayments).setHeader("# Bezahlungen");
        grid.addColumn(dateRenderer(dto -> dto.lastPayment().orElse(null))).setHeader("Letzte Bezahlung");
        if (authenticatedUser.hasWritePermission(PASSIVMITGLIED_GROUP)) {
            grid.addColumn(clickableIcon(EDIT,
                                         dto -> PassivmitgliedDialog.show(dto, newPayments -> {
                                             passivmitgliedService.addPayments(dto.id(), newPayments, authenticatedUser.getName());
                                             dataProvider.refreshAll();
                                         })));
        }
        if (authenticatedUser.hasReadPermission(PASSIVMITGLIED_GROUP)) {
            grid.addColumn(clickableIcon(EYE, PassivmitgliedDialog::showReadOnly));
        }

        dataProvider = passivmitgliedService.dataProvider();
        grid.setDataProvider(dataProvider);
    }
}
