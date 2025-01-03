package ch.mvurdorf.platform.home;

import ch.mvurdorf.platform.konzerte.KonzerteService;
import ch.mvurdorf.platform.utils.FormatUtil;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
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
            var accordion = new Accordion();
            for (var konzert : futureKonzerte) {
                var content = new VerticalLayout();
                content.setSpacing(false);
                content.setPadding(false);
                content.add(new Paragraph("%s, %s".formatted(FormatUtil.formatDateTime(konzert.datumZeit()), konzert.location())));
                if (isNotBlank(konzert.description())) {
                    content.add(new Paragraph(konzert.description()));
                }
                int i = 1;
                for (var entry : konzert.entries()) {
                    if (entry.isPlaceholder()) {
                        content.add(new Div(entry.titel()));
                    } else {
                        content.add(new Div("%d. %s".formatted(i, entry.titel())));
                        i++;
                    }
                }
                accordion.add(new AccordionPanel(konzert.name(), content));
            }
            add(accordion);
        }
    }

}
