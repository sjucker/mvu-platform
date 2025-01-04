package ch.mvurdorf.platform.repertoire;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import static ch.mvurdorf.platform.security.LoginService.REPERTOIRE_GROUP;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.VOLUME_UP_SOLID;

@PageTitle("Repertoire")
@Route("repertoire")
@RolesAllowed({REPERTOIRE_GROUP})
@Menu(order = 3, icon = VOLUME_UP_SOLID)
public class RepertoireView extends VerticalLayout {

    private final RepertoireService repertoireService;

    public RepertoireView(RepertoireService repertoireService) {
        this.repertoireService = repertoireService;

        setSizeFull();
    }
}
