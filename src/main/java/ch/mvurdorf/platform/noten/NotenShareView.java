package ch.mvurdorf.platform.noten;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@AnonymousAllowed
@PageTitle("Noten teilen")
@Route(value = "noten-share/:token")
@RequiredArgsConstructor
public class NotenShareView extends VerticalLayout implements BeforeEnterObserver {

    private final NotenShareService shareService;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        removeAll();

        var tokenOpt = event.getRouteParameters().get("token");
        if (tokenOpt.isEmpty()) {
            add(new H2("Ungültiger Link"), new Paragraph("Kein Token angegeben."));
            return;
        }

        UUID token;
        try {
            token = UUID.fromString(tokenOpt.get());
        } catch (Exception e) {
            add(new H2("Ungültiger Link"), new Paragraph("Das bereitgestellte Token ist nicht gültig."));
            return;
        }

        // Header
        add(new H2("Geteilte Noten"));

        // Actions row
        var mergedUrl = "/api/noten-share/" + token + "/all.pdf";
        var mergedAnchor = new Anchor(mergedUrl, "Alle als PDF öffnen");
        mergedAnchor.setTarget("_blank");
        var actions = new HorizontalLayout(mergedAnchor);
        add(actions);

        try {
            var list = shareService.listPdfsForToken(token);
            if (list.isEmpty()) {
                add(new Paragraph("Für diesen Link sind keine Noten verfügbar."));
                return;
            }

            var container = new VerticalLayout();
            container.setPadding(false);
            container.setSpacing(false);

            list.forEach(item -> {
                var singleUrl = "/api/noten-share/" + token + "/" + item.getId() + ".pdf";
                var anchor = new Anchor(singleUrl, item.getTitel());
                anchor.setTarget("_blank");
                container.add(anchor);
            });

            add(new H2("Einzeldownloads"));
            add(container);
        } catch (Exception e) {
            add(new H2("Link nicht verfügbar"), new Paragraph("Der Link ist abgelaufen, deaktiviert oder ungültig."));
        }
    }
}
