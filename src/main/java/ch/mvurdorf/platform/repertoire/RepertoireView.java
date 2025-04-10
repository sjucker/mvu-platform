package ch.mvurdorf.platform.repertoire;

import ch.mvurdorf.platform.noten.KompositionService;
import ch.mvurdorf.platform.security.AuthenticatedUser;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;

import static ch.mvurdorf.platform.repertoire.RepertoireType.KONZERTMAPPE;
import static ch.mvurdorf.platform.repertoire.RepertoireType.MARSCHBUCH;
import static ch.mvurdorf.platform.security.LoginService.REPERTOIRE_GROUP;
import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static ch.mvurdorf.platform.ui.ComponentUtil.secondaryButton;
import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static ch.mvurdorf.platform.ui.RendererUtil.repertoireNumber;
import static ch.mvurdorf.platform.utils.FormatUtil.formatDateTime;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.BASELINE;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.VOLUME_UP_SOLID;

@PageTitle("Repertoire")
@Route("repertoire")
@RolesAllowed({REPERTOIRE_GROUP})
@Menu(order = 3, icon = VOLUME_UP_SOLID)
public class RepertoireView extends VerticalLayout {

    private final RepertoireService repertoireService;
    private final KompositionService kompositionService;
    private final AuthenticatedUser authenticatedUser;

    private TabSheet tabs;

    public RepertoireView(RepertoireService repertoireService, KompositionService kompositionService, AuthenticatedUser authenticatedUser) {
        this.repertoireService = repertoireService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        createTabs();
        add(tabs);
        this.kompositionService = kompositionService;
    }

    private void createTabs() {
        tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.add(new Tab("Konzertmappe"), createTab(KONZERTMAPPE));
        tabs.add(new Tab("Marschbuch"), createTab(MARSCHBUCH));
    }

    private VerticalLayout createTab(RepertoireType repertoireType) {
        var layout = new VerticalLayout();
        layout.setSizeFull();

        var entries = new ArrayList<RepertoireEntryDto>();

        var details = new TextArea("Informationen");
        details.setWidthFull();

        var grid = new Grid<RepertoireEntryDto>();

        var save = secondaryButton("Speichern", () -> {
            repertoireService.save(entries, repertoireType, details.getValue(), authenticatedUser.getName());
            repertoireService.findRepertoireByType(repertoireType).ifPresent(it -> {
                entries.clear();
                entries.addAll(it.entries());
                grid.setItems(entries);
                Notification.show("Speichern erfolgreich.", 3000, TOP_CENTER);
            });
        });
        save.setEnabled(false);
        save.setDisableOnClick(true);

        var repertoire = repertoireService.findRepertoireByType(repertoireType);

        details.addValueChangeListener(_ -> save.setEnabled(true));

        var controls = new HorizontalLayout();
        controls.setWidthFull();
        controls.addClassName(Gap.LARGE);
        controls.setAlignItems(BASELINE);
        var add = primaryButton("Hinzufügen", () -> {
            RepertoireEntryAddDialog.show(kompositionService, newEntry -> {
                entries.add(newEntry);
                entries.sort(comparing(RepertoireEntryDto::number, nullsLast(naturalOrder())));
                grid.setItems(entries);
                save.setEnabled(true);
            });
        });
        controls.add(add, save);
        repertoire.ifPresent(it -> {
            var info = new Span("Letzte Aktualisierung am: %s durch %s".formatted(formatDateTime(it.createdAt()), it.createdBy()));
            info.addClassName(FontSize.SMALL);
            controls.add(info);
        });

        grid.setSizeFull();
        grid.addColumn(repertoireNumber(RepertoireEntryDto::number)).setWidth("80px").setFlexGrow(0);
        grid.addColumn(RepertoireEntryDto::label);
        grid.addColumn(clickableIcon(TRASH, dto -> new ConfirmDialog("%s löschen?".formatted(dto.kompositionTitel()), "Sicher?",
                                                                     "Löschen",
                                                                     _ -> {
                                                                         entries.remove(dto);
                                                                         grid.setItems(entries);
                                                                         save.setEnabled(true);
                                                                     },
                                                                     "Abbrechen",
                                                                     _ -> {
                                                                     }).open()))
            .setWidth("60px").setFlexGrow(0);

        repertoire.ifPresent(it -> {
            entries.addAll(it.entries());
            grid.setItems(entries);
            if (isNotBlank(it.details())) {
                details.setValue(it.details());
            }
        });

        layout.add(controls, details, grid);
        return layout;
    }
}
