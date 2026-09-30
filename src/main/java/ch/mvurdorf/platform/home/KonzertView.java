package ch.mvurdorf.platform.home;

import ch.mvurdorf.platform.konzerte.KonzertEntryDto;
import ch.mvurdorf.platform.konzerte.KonzertSetlistPdfService;
import ch.mvurdorf.platform.konzerte.KonzerteService;
import ch.mvurdorf.platform.noten.NotenService;
import ch.mvurdorf.platform.security.AuthenticatedUser;
import ch.mvurdorf.platform.service.StorageService;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;

import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static ch.mvurdorf.platform.ui.RendererUtil.externalLink;
import static ch.mvurdorf.platform.ui.RendererUtil.iconPopover;
import static ch.mvurdorf.platform.ui.RendererUtil.repertoireNumber;
import static com.vaadin.flow.component.html.AttachmentType.DOWNLOAD;
import static com.vaadin.flow.component.icon.VaadinIcon.DOWNLOAD_ALT;
import static com.vaadin.flow.component.icon.VaadinIcon.FILE_SOUND;
import static com.vaadin.flow.component.icon.VaadinIcon.INFO_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.MUSIC;
import static com.vaadin.flow.theme.lumo.LumoUtility.Whitespace.PRE_WRAP;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@PageTitle("Konzert")
@Route("konzert")
@PermitAll
public class KonzertView extends VerticalLayout implements HasUrlParameter<Long> {

    private final KonzerteService konzerteService;
    private final KonzertSetlistPdfService konzertSetlistPdfService;
    private final NotenService notenService;
    private final StorageService storageService;
    private final AuthenticatedUser authenticatedUser;

    public KonzertView(KonzerteService konzerteService,
                       KonzertSetlistPdfService konzertSetlistPdfService,
                       NotenService notenService,
                       StorageService storageService,
                       AuthenticatedUser authenticatedUser) {
        this.konzerteService = konzerteService;
        this.konzertSetlistPdfService = konzertSetlistPdfService;
        this.notenService = notenService;
        this.storageService = storageService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long konzertId) {
        konzerteService.findById(konzertId).ifPresentOrElse(
                konzertDto -> {
                    add(new H2(konzertDto.name()));
                    add(new H3(konzertDto.dateTimeAndLocation()));
                    if (isNotBlank(konzertDto.description())) {
                        var description = new Paragraph(konzertDto.description());
                        description.addClassName(PRE_WRAP);
                        add(description);
                    }
                    if (isNotBlank(konzertDto.tenu())) {
                        var tenu = new Paragraph("Tenü: " + konzertDto.tenu());
                        tenu.addClassName(PRE_WRAP);
                        add(tenu);
                    }
                    if (!konzertDto.entries().isEmpty()) {
                        var setlistDownload = new Anchor(DownloadHandler.fromInputStream(_ -> new DownloadResponse(new ByteArrayInputStream(konzertSetlistPdfService.exportSetlist(konzertDto)),
                                                                                                                   "Setlist %s.pdf".formatted(konzertDto.name()), "application/pdf", -1)),
                                                         DOWNLOAD, "");
                        setlistDownload.add(new Button("Setlist als PDF", DOWNLOAD_ALT.create()));
                        add(setlistDownload);
                    }

                    var entries = new Grid<KonzertEntryDto>();
                    entries.setSelectionMode(SelectionMode.NONE);

                    if (konzertDto.hasMarschbuchEntry()) {
                        entries.addColumn(repertoireNumber(KonzertEntryDto::getMarschbuchNumber))
                               .setHeader("Marschbuch")
                               .setWidth("120px").setFlexGrow(0);
                    }

                    entries.addColumn(iconPopover(INFO_CIRCLE, KonzertEntryDto::getAdditionalInfo))
                           .setWidth("60px").setFlexGrow(0);

                    entries.addColumn(new ComponentRenderer<>(dto -> {
                               var titel = new Span(dto.titel());
                               if (dto.isZugabe()) {
                                   var zugabe = new Badge("Zugabe");
                                   zugabe.addThemeVariants(BadgeVariant.SMALL);
                                   titel.add(zugabe);
                                   titel.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.SMALL);
                               }
                               return titel;
                           }))
                           .setHeader("Titel")
                           .setFlexGrow(1);

                    entries.addColumn(clickableIcon(MUSIC,
                                                    dto -> NotenDownloadDialog.show(notenService, storageService, authenticatedUser.getInstrumentPermissions(), dto.getKompositionId(), dto.getKompositionTitel()),
                                                    dto -> !dto.isPlaceholderEntry(),
                                                    "Noten-Download"))
                           .setWidth("60px").setFlexGrow(0);

                    entries.addColumn(externalLink(FILE_SOUND, KonzertEntryDto::getKompositionAudioSample, "Hörprobe"))
                           .setWidth("60px").setFlexGrow(0);

                    entries.setItems(konzertDto.entries());
                    add(entries);
                },
                () -> add(new Paragraph("Konzert nicht gefunden.")));
    }
}
