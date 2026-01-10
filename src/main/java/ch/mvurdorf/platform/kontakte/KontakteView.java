package ch.mvurdorf.platform.kontakte;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import static org.vaadin.lineawesome.LineAwesomeIconUrl.ADDRESS_BOOK;

@PageTitle("Kontakte")
@Route(value = "kontakte")
@PermitAll
@Menu(order = 9, icon = ADDRESS_BOOK)
public class KontakteView extends VerticalLayout {

    private final KontakteService kontakteService;

    public KontakteView(KontakteService kontakteService) {
        this.kontakteService = kontakteService;

        setSizeFull();
        add(createMailinglistenSection(), createAktivmitgliederSection());
    }

    private VerticalLayout createAktivmitgliederSection() {
        var layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setSizeFull();

        layout.add(new H3("Aktivmitglieder"), createGrid());

        return layout;
    }

    private VerticalLayout createMailinglistenSection() {
        var layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        layout.add(new H3("Mailinglisten"));
        layout.add(new Anchor("mailto:alle@mvurdorf.ch", "alle@mvurdorf.ch"));
        layout.add(new Anchor("mailto:muko@mvurdorf.ch", "muko@mvurdorf.ch"));

        return layout;
    }

    private Grid<KontaktDto> createGrid() {
        var grid = new Grid<KontaktDto>();
        grid.setSizeFull();

        grid.addColumn(KontaktDto::name).setHeader("Name");
        grid.addComponentColumn(kontakt -> {
            var email = kontakt.email();
            return new Anchor("mailto:" + email, email);
        }).setHeader("Email");
        grid.addColumn(kontaktDto -> kontaktDto.register().getDescription()).setHeader("Register");

        grid.setItems(kontakteService.findActiveUsers());

        return grid;
    }
}
