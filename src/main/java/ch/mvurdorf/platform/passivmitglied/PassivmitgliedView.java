package ch.mvurdorf.platform.passivmitglied;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import static ch.mvurdorf.platform.security.LoginService.PASSIVMITGLIED_GROUP;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.MONEY_BILL_ALT;

@PageTitle("Passivmitglieder")
@Route("passivmitglied")
@RolesAllowed({PASSIVMITGLIED_GROUP})
@Menu(order = 3, icon = MONEY_BILL_ALT)
public class PassivmitgliedView extends VerticalLayout {

    private final PassivmitgliedService passivmitgliedService;

    private Grid<PassivmitgliedDto> grid;

    public PassivmitgliedView(PassivmitgliedService passivmitgliedService) {
        this.passivmitgliedService = passivmitgliedService;

        setSizeFull();
        createGrid();
        add(grid);
    }

    private void createGrid() {
        grid = new Grid<>();

        grid.addColumn(PassivmitgliedDto::vorname).setHeader("Vorname");
        grid.addColumn(PassivmitgliedDto::nachname).setHeader("Nachname");
        grid.addColumn(new LocalDateRenderer<>(dto -> dto.getLastPayment().orElse(null), "dd.MM.yyyy")).setHeader("Letzte Bezahlung");

        grid.setItems(passivmitgliedService.findAll());
    }
}
