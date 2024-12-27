package ch.mvurdorf.platform.passivmitglied;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;

@PageTitle("Passivmitglied")

@Route(value = "portal", autoLayout = false)
@AnonymousAllowed
public class PassivmitgliedPortalView extends AppLayout implements HasUrlParameter<String> {

    private final PassivmitgliedService passivmitgliedService;

    public PassivmitgliedPortalView(PassivmitgliedService passivmitgliedService) {
        this.passivmitgliedService = passivmitgliedService;

        var title = new H3("Passivmitglied");
        title.addClassName(Padding.MEDIUM);
        addToNavbar(title);
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        var passivmitglied = passivmitgliedService.findByUUID(parameter);
        if (passivmitglied.isPresent()) {
            var tabs = new TabSheet();
            tabs.setSizeFull();
            tabs.add(new Tab("Test"), new HorizontalLayout(new H4("Test")));
            tabs.add(new Tab("Test 2"), new HorizontalLayout(new H4("Test 2")));
            // TODO
            setContent(tabs);
        } else {
            setContent(new VerticalLayout(new Paragraph("Passivmitglied nicht gefunden.")));

        }
    }

}
