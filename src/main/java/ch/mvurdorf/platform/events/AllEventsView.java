package ch.mvurdorf.platform.events;

import ch.mvurdorf.platform.security.AuthenticatedUser;
import ch.mvurdorf.platform.ui.CardContainer;
import ch.mvurdorf.platform.ui.EventCard;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("Nächste Termine")
@Route("termine")
@PermitAll
public class AllEventsView extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;
    private final EventsService eventsService;

    public AllEventsView(AuthenticatedUser authenticatedUser, EventsService eventsService) {
        this.authenticatedUser = authenticatedUser;
        this.eventsService = eventsService;

        setSizeFull();
        create();
    }

    private void create() {
        var container = new CardContainer();
        eventsService.findEventAbsenzenForUser(authenticatedUser.getId())
                     .forEach(event -> container.add(new EventCard(event, eventsService)));
        add(container);
    }
}
