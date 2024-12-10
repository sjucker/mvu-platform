package ch.mvurdorf.platform.repertoire;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import static org.vaadin.lineawesome.LineAwesomeIconUrl.LIST_ALT;

@PageTitle("Repertoire")
@Route("repertoire")
@RolesAllowed({"ADMIN"})
@Menu(order = 3, icon = LIST_ALT)
public class RepertoireView extends VerticalLayout {

    public RepertoireView() {

        setSizeFull();
    }
}
