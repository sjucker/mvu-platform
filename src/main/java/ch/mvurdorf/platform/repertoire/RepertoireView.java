package ch.mvurdorf.platform.repertoire;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import static ch.mvurdorf.platform.repertoire.RepertoireType.KONZERTMAPPE;
import static ch.mvurdorf.platform.security.LoginService.REPERTOIRE_GROUP;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.VOLUME_UP_SOLID;

@PageTitle("Repertoire")
@Route("repertoire")
@RolesAllowed({REPERTOIRE_GROUP})
@Menu(order = 3, icon = VOLUME_UP_SOLID)
public class RepertoireView extends VerticalLayout {

    private final RepertoireService repertoireService;

    private TabSheet tabs;

    public RepertoireView(RepertoireService repertoireService) {
        this.repertoireService = repertoireService;

        setSizeFull();
        createTabs();
        add(tabs);
    }

    private void createTabs() {
        tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.add(new Tab("Konzertmappe"), createTabKonzertmappe());
        tabs.add(new Tab("Marschbuch"), createTabMarschbuch());
    }

    private VerticalLayout createTabMarschbuch() {
        return new VerticalLayout();
    }

    private VerticalLayout createTabKonzertmappe() {
        var layout = new VerticalLayout();
        layout.setSizeFull();
        var repertoire = repertoireService.findRepertoireByType(KONZERTMAPPE);

        var grid = new Grid<RepertoireEntryDto>();
        grid.setSizeFull();
        grid.addColumn(RepertoireEntryDto::number).setWidth("100px");
        grid.addColumn(RepertoireEntryDto::label);

        repertoire.ifPresent(it -> grid.setItems(it.entries()));

        layout.add(grid);
        return layout;
    }
}
