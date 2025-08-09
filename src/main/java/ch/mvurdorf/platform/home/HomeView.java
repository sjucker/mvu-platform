package ch.mvurdorf.platform.home;

import ch.mvurdorf.platform.common.AbsenzState;
import ch.mvurdorf.platform.common.LocalizedEnum;
import ch.mvurdorf.platform.events.EventsService;
import ch.mvurdorf.platform.konzerte.KonzerteService;
import ch.mvurdorf.platform.repertoire.RepertoireService;
import ch.mvurdorf.platform.repertoire.RepertoireType;
import ch.mvurdorf.platform.security.AuthenticatedUser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import static ch.mvurdorf.platform.common.AbsenzState.INACTIVE;
import static ch.mvurdorf.platform.common.AbsenzState.NEGATIVE;
import static ch.mvurdorf.platform.common.AbsenzState.POSITIVE;
import static ch.mvurdorf.platform.repertoire.RepertoireType.KONZERTMAPPE;
import static ch.mvurdorf.platform.repertoire.RepertoireType.MARSCHBUCH;
import static ch.mvurdorf.platform.ui.ComponentUtil.secondaryButton;
import static ch.mvurdorf.platform.utils.FormatUtil.formatDateTime;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.HOME_SOLID;

@PageTitle("Home")
@Route("")
@PermitAll
@Menu(order = 0, icon = HOME_SOLID)
public class HomeView extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;
    private final KonzerteService konzerteService;
    private final EventsService eventsService;
    private final RepertoireService repertoireService;

    public HomeView(AuthenticatedUser authenticatedUser, KonzerteService konzerteService, EventsService eventsService, RepertoireService repertoireService) {
        this.authenticatedUser = authenticatedUser;
        this.konzerteService = konzerteService;
        this.eventsService = eventsService;
        this.repertoireService = repertoireService;

        setSizeFull();
        create();
    }

    private void create() {
        add(new H3("Nächste Termine"));
        var absenzen = cardContainer();
        eventsService.findEventAbsenzenForUser(authenticatedUser.getId()).stream().limit(4).forEach(event -> {
            var card = new Card();
            card.setTitle(new Div(event.title()));
            card.setSubtitle(new Div(event.subtitle()));
            var absenzStatus = new RadioButtonGroup<AbsenzState>();
            absenzStatus.setItems(POSITIVE, NEGATIVE, INACTIVE);
            absenzStatus.setItemLabelGenerator(LocalizedEnum::getDescription);
            absenzStatus.setValue(event.status());
            absenzStatus.setWidthFull();

            var remark = new TextField();
            remark.setPlaceholder("Bemerkung");
            remark.setValue(defaultString(event.remark()));
            remark.setWidthFull();

            absenzStatus.addValueChangeListener(e -> eventsService.updateEventAbsenzenForUser(event.loginId(), event.eventId(), e.getValue(), remark.getValue()));
            remark.addValueChangeListener(e -> eventsService.updateEventAbsenzenForUser(event.loginId(), event.eventId(), absenzStatus.getValue(), e.getValue()));

            var verticalLayout = new VerticalLayout(absenzStatus, remark);
            verticalLayout.setPadding(false);
            verticalLayout.setSpacing(false);
            card.addToFooter(verticalLayout);

            absenzen.add(card);
        });
        add(absenzen);

        add(new Hr());
        add(new H3("Nächste Konzerte"));
        var futureKonzerte = konzerteService.getFutureKonzerte();
        if (futureKonzerte.isEmpty()) {
            add(new Paragraph("Momentan keine zukünftige Konzerte erfasst."));
        } else {
            var konzerte = cardContainer();
            for (var konzert : futureKonzerte) {
                var card = new Card();
                card.setTitle(new Div(konzert.name()));
                card.setSubtitle(new Div(konzert.dateTimeAndLocation()));
                card.addToFooter(secondaryButton("Details", () -> UI.getCurrent().navigate(KonzertView.class, konzert.id())));
                konzerte.add(card);
            }
            add(konzerte);
        }

        add(new Hr());
        add(new H3("Repertoire"));
        var repertoire = cardContainer();

        repertoire.add(repertoireCard(KONZERTMAPPE));
        repertoire.add(repertoireCard(MARSCHBUCH));

        add(repertoire);
    }

    private Card repertoireCard(RepertoireType repertoireType) {
        var card = new Card();
        card.setTitle(new Div(repertoireType.getDescription()));
        card.setSubtitle(new Div(repertoireService.findRepertoireByType(repertoireType)
                                                  .map(repertoire -> "vom %s".formatted(formatDateTime(repertoire.createdAt())))
                                                  .orElse("")));
        card.addToFooter(secondaryButton("Details", () -> UI.getCurrent().navigate(RepertoireDetailView.class, repertoireType.name())));
        return card;
    }

    private Div cardContainer() {
        var div = new Div();
        div.setWidthFull();
        div.getStyle().set("display", "grid")
           .set("grid-template-columns", "repeat(auto-fill, minmax(350px, 1fr))")
           .set("gap", "1em");
        return div;
    }

}
