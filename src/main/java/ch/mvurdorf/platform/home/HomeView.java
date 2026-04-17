package ch.mvurdorf.platform.home;

import ch.mvurdorf.platform.events.AllEventsView;
import ch.mvurdorf.platform.events.EventsService;
import ch.mvurdorf.platform.jooq.tables.pojos.Login;
import ch.mvurdorf.platform.konzerte.KonzerteService;
import ch.mvurdorf.platform.repertoire.RepertoireService;
import ch.mvurdorf.platform.repertoire.RepertoireType;
import ch.mvurdorf.platform.security.AuthenticatedUser;
import ch.mvurdorf.platform.ui.CardContainer;
import ch.mvurdorf.platform.ui.EventCard;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import jakarta.annotation.security.PermitAll;

import static ch.mvurdorf.platform.repertoire.RepertoireType.KONZERTMAPPE;
import static ch.mvurdorf.platform.repertoire.RepertoireType.MARSCHBUCH;
import static ch.mvurdorf.platform.ui.ComponentUtil.secondaryButton;
import static ch.mvurdorf.platform.utils.FormatUtil.formatDateTime;
import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_SUCCESS;
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
        var absenzen = new CardContainer();
        eventsService.findEventAbsenzenForUser(authenticatedUser.getId()).stream()
                     .limit(4)
                     .forEach(event -> absenzen.add(new EventCard(event, eventsService)));
        add(absenzen);
        add(secondaryButton("Alle anzeigen", () -> UI.getCurrent().navigate(AllEventsView.class)));

        add(new Hr());
        add(new H3("Nächste Konzerte"));
        var futureKonzerte = konzerteService.getFutureKonzerte();
        if (futureKonzerte.isEmpty()) {
            add(new Paragraph("Momentan keine zukünftige Konzerte erfasst."));
        } else {
            var konzerte = new CardContainer();
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
        var repertoire = new CardContainer();
        repertoire.add(repertoireCard(KONZERTMAPPE));
        repertoire.add(repertoireCard(MARSCHBUCH));
        add(repertoire);

        authenticatedUser.get()
                         .map(Login::getCalendarToken)
                         .ifPresent(this::addCalendarSection);

        add(new Hr());
        add(new H3("App"));

        var android = getAppDownloadButton("images/google-play.png", "Google Play", "https://play.google.com/store/apps/details?id=ch.mvurdorf.platform");
        var ios = getAppDownloadButton("images/apple-store.png", "App Store", "https://apps.apple.com/app/musikverein-harmonie-urdorf/id6749552122");

        var appContainer = new FlexLayout(android, ios);
        appContainer.addClassNames(FlexDirection.COLUMN, FlexDirection.Breakpoint.Medium.ROW, JustifyContent.CENTER, Gap.MEDIUM);
        add(appContainer);
    }

    private void addCalendarSection(String calendarToken) {
        var container = new Div(new Hr(),
                                new H3("Mein Kalender"),
                                new Paragraph("Deine persönlichen Termine (bei denen du als «anwesend» eingetragen bist) können als Kalender-Abo eingebunden werden. Kopiere die URL und füge sie in deiner Kalender-App ein:"),
                                new UnorderedList(
                                        new ListItem("Google Calendar: Weitere Kalender → + → Per URL"),
                                        new ListItem("Outlook: Kalender hinzufügen → Aus dem Internet abonnieren → URL einfügen"),
                                        new ListItem("Apple Kalender: Ablage → Neues Kalenderabonnement → URL einfügen")
                                ));

        var urlField = new TextField();
        urlField.setReadOnly(true);
        urlField.setWidthFull();

        var copyButton = new Button(new Icon(VaadinIcon.CLIPBOARD), _ -> {
            UI.getCurrent().getPage().executeJs("navigator.clipboard.writeText($0)", urlField.getValue());
            Notification.show("URL kopiert!", 2000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(LUMO_SUCCESS);
        });
        copyButton.addThemeVariants(LUMO_TERTIARY);
        copyButton.setTooltipText("URL kopieren");

        var urlRow = new HorizontalLayout(urlField, copyButton);
        urlRow.setWidthFull();
        urlRow.setAlignItems(Alignment.BASELINE);
        container.add(urlRow);

        UI.getCurrent().getPage().fetchCurrentURL(url -> {
            var origin = url.getProtocol() + "://" + url.getHost() + (url.getPort() != -1 ? ":" + url.getPort() : "");
            urlField.setValue(origin + "/api/calendar/" + calendarToken + "/events.ics");
        });

        add(container);
    }

    private Anchor getAppDownloadButton(String src, String alt, String url) {
        var img = new Image(src, alt);
        img.setHeight("65px");
        return new Anchor(url, img);
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

}
