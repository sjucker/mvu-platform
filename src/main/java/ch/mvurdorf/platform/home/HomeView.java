package ch.mvurdorf.platform.home;

import ch.mvurdorf.platform.konzerte.KonzerteService;
import ch.mvurdorf.platform.ui.StyleUtility.Cursor;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility.Border;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderColor;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius;
import jakarta.annotation.security.PermitAll;

import static ch.mvurdorf.platform.repertoire.RepertoireType.KONZERTMAPPE;
import static ch.mvurdorf.platform.repertoire.RepertoireType.MARSCHBUCH;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.HOME_SOLID;

@PageTitle("Home")
@Route("")
@PermitAll
@Menu(order = 0, icon = HOME_SOLID)
public class HomeView extends VerticalLayout {

    private final KonzerteService konzerteService;

    public HomeView(KonzerteService konzerteService) {
        this.konzerteService = konzerteService;

        setSizeFull();
        create();
    }

    private void create() {
        add(new H3("Nächste Konzerte"));
        var futureKonzerte = konzerteService.getFutureKonzerte();
        if (futureKonzerte.isEmpty()) {
            add(new Paragraph("Momentan keine zukünftige Konzerte erfasst."));
        } else {
            for (var konzert : futureKonzerte) {
                var card = new VerticalLayout();
                card.setPadding(true);
                card.setSpacing(false);
                card.add(new H6(konzert.dateTimeAndLocation()));
                card.add(new H4(konzert.name()));
                card.addClassNames(Border.ALL, BorderColor.CONTRAST_90, BorderRadius.MEDIUM, Cursor.POINTER);
                card.addClickListener(_ -> UI.getCurrent().navigate(KonzertView.class, konzert.id()));
                add(card);
            }
        }

        add(new Hr());
        add(new H3("Repertoire"));
        add(new RouterLink(KONZERTMAPPE.getDescription(), RepertoireDetailView.class, KONZERTMAPPE.name()));
        add(new RouterLink(MARSCHBUCH.getDescription(), RepertoireDetailView.class, MARSCHBUCH.name()));
    }

}
