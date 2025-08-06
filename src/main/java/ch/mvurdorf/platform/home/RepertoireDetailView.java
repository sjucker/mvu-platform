package ch.mvurdorf.platform.home;

import ch.mvurdorf.platform.noten.NotenService;
import ch.mvurdorf.platform.repertoire.RepertoireEntryDto;
import ch.mvurdorf.platform.repertoire.RepertoireService;
import ch.mvurdorf.platform.repertoire.RepertoireType;
import ch.mvurdorf.platform.security.AuthenticatedUser;
import ch.mvurdorf.platform.service.StorageService;
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
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Whitespace;
import jakarta.annotation.security.PermitAll;

import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static ch.mvurdorf.platform.ui.RendererUtil.externalLink;
import static ch.mvurdorf.platform.ui.RendererUtil.repertoireNumber;
import static ch.mvurdorf.platform.utils.FormatUtil.formatDateTime;
import static com.vaadin.flow.component.icon.VaadinIcon.FILE_SOUND;
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
                                                  details.addClassName(Whitespace.PRE_WRAP);
                                                  add(details);
                                              }

                                              var lastUpdated = new Span("Letzte Änderung am: %s durch %s".formatted(formatDateTime(repertoire.createdAt()), repertoire.createdBy()));
                                              lastUpdated.addClassName(FontSize.XSMALL);

                                              var controls = new HorizontalLayout(lastUpdated,
                                                                                  primaryButton("Alle PDFs herunterladen", () -> ExportAllPdfDialog.show(notenService, authenticatedUser, repertoire)));
                                              controls.setWidthFull();
                                              controls.addClassNames(Display.FLEX, FlexDirection.COLUMN, FlexDirection.Breakpoint.Medium.ROW,
                                                                     JustifyContent.BETWEEN, AlignItems.BASELINE);
                                              add(controls);

                                              var entries = new Grid<RepertoireEntryDto>();
                                              entries.addColumn(clickableIcon(MUSIC,
                                                                              dto -> NotenDownloadDialog.show(notenService, storageService, authenticatedUser.getInstrumentPermissions(), dto.kompositionId(), dto.kompositionTitel())))
                                                     .setWidth("60px").setFlexGrow(0);
                                              entries.addColumn(repertoireNumber(RepertoireEntryDto::number))
                                                     .setWidth("100px").setFlexGrow(0);
                                              entries.addColumn(RepertoireEntryDto::label);
                                              entries.addColumn(externalLink(FILE_SOUND, RepertoireEntryDto::kompositionAudioSample))
                                                     .setWidth("60px").setFlexGrow(0);
                                              entries.setItems(repertoire.entries());
                                              add(entries);
                                          },
                                          () -> add(new Paragraph("Kein Repertoire gefunden")));
    }

}
