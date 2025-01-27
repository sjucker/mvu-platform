package ch.mvurdorf.platform.home;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.noten.NotenService;
import ch.mvurdorf.platform.repertoire.RepertoireDto;
import ch.mvurdorf.platform.repertoire.RepertoireEntryDto;
import ch.mvurdorf.platform.repertoire.RepertoireService;
import ch.mvurdorf.platform.repertoire.RepertoireType;
import ch.mvurdorf.platform.security.AuthenticatedUser;
import ch.mvurdorf.platform.service.StorageService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;

import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static ch.mvurdorf.platform.utils.BigDecimalUtil.formatBigDecimal;
import static ch.mvurdorf.platform.utils.FormatUtil.formatDateTime;
import static com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY;
import static com.vaadin.flow.component.icon.VaadinIcon.MUSIC;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@PageTitle("Repertoire")
@Route("repertoire-detail")
@PermitAll
public class RepertoireDetailView extends VerticalLayout implements HasUrlParameter<String> {

    private final RepertoireService repertoireService;
    private final NotenService notenService;
    private final StorageService storageService;
    private final AuthenticatedUser authenticatedUser;

    public RepertoireDetailView(RepertoireService repertoireService,
                                NotenService notenService,
                                StorageService storageService,
                                AuthenticatedUser authenticatedUser) {
        this.repertoireService = repertoireService;
        this.notenService = notenService;
        this.storageService = storageService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        var type = RepertoireType.valueOf(parameter);
        repertoireService.findRepertoireByType(type)
                         .ifPresentOrElse(repertoire -> {
                                              add(new H2(type.getDescription()));
                                              if (isNotBlank(repertoire.details())) {
                                                  var details = new Paragraph(repertoire.details());
                                                  details.addClassName(LumoUtility.Whitespace.PRE_WRAP);
                                                  add(details);
                                              }

                                              var controls = new HorizontalLayout(new Span("Letzte Änderung am: %s".formatted(formatDateTime(repertoire.createdAt()))),
                                                                                  createExportAllButton(repertoire));
                                              controls.setWidthFull();
                                              controls.setJustifyContentMode(JustifyContentMode.BETWEEN);
                                              controls.setAlignItems(Alignment.BASELINE);
                                              add(controls);

                                              var entries = new Grid<RepertoireEntryDto>();
                                              entries.addColumn(dto -> formatBigDecimal(dto.number()))
                                                     .setWidth("100px").setFlexGrow(0);
                                              entries.addColumn(RepertoireEntryDto::label);
                                              entries.addColumn(clickableIcon(MUSIC,
                                                                              dto -> NotenDownloadDialog.show(notenService, storageService, authenticatedUser.getInstrumentPermissions(), dto.kompositionId(), dto.kompositionTitel()))).setWidth("60px").setFlexGrow(0);
                                              entries.setItems(repertoire.entries());
                                              add(entries);
                                          },
                                          () -> add(new Paragraph("Kein Repertoire gefunden")));
    }

    private Button createExportAllButton(RepertoireDto repertoire) {
        var exportAll = new Button("Alle PDFs herunterladen");
        exportAll.addThemeVariants(LUMO_PRIMARY);

        var instrumentPermissions = authenticatedUser.getInstrumentPermissions();
        if (instrumentPermissions.size() == 1) {
            // user is allowed to see PDfs of single instrument, export immediately, otherwise provide instrument-selection
            exportAll.addClickListener(_ -> exportAll(repertoire, instrumentPermissions.stream().findFirst().orElseThrow()));
        } else {
            var contextMenu = new ContextMenu();
            contextMenu.setTarget(exportAll);
            contextMenu.setOpenOnClick(true);

            if (instrumentPermissions.isEmpty()) {
                // users has no permission/restriction, sees all
                contextMenu.addItem("Alle", _ -> exportAll(repertoire, null));
            }

            for (var instrument : Instrument.values()) {
                if (instrumentPermissions.isEmpty() || instrumentPermissions.contains(instrument)) {
                    contextMenu.addItem(instrument.getDescription(), _ -> exportAll(repertoire, instrument));
                }
            }
        }

        return exportAll;
    }

    private void exportAll(RepertoireDto repertoire, Instrument instrument) {
        var bytes = instrument != null ?
                notenService.exportNotenToPdf(repertoire.kompositionIds(), instrument) :
                notenService.exportNotenToPdf(repertoire.kompositionIds());

        var resource = new StreamResource("noten.pdf", (InputStreamFactory) () -> new ByteArrayInputStream(bytes));
        var registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().open(registration.getResourceUri().toString());
    }
}
