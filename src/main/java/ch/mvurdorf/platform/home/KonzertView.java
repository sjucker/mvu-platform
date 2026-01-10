package ch.mvurdorf.platform.home;

import ch.mvurdorf.platform.konzerte.KonzertEntryDto;
import ch.mvurdorf.platform.konzerte.KonzerteService;
import ch.mvurdorf.platform.noten.NotenService;
import ch.mvurdorf.platform.security.AuthenticatedUser;
import ch.mvurdorf.platform.service.StorageService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import static ch.mvurdorf.platform.konzerte.KonzerteService.getNumber;
import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static ch.mvurdorf.platform.ui.RendererUtil.externalLink;
import static ch.mvurdorf.platform.ui.RendererUtil.iconPopover;
import static ch.mvurdorf.platform.ui.RendererUtil.repertoireNumber;
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
    private final NotenService notenService;
    private final StorageService storageService;
    private final AuthenticatedUser authenticatedUser;

    public KonzertView(KonzerteService konzerteService, NotenService notenService, StorageService storageService, AuthenticatedUser authenticatedUser) {
        this.konzerteService = konzerteService;
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
                    var entries = new Grid<KonzertEntryDto>();
                    entries.setSelectionMode(SelectionMode.NONE);

                    entries.addColumn(dto -> getNumber(dto, konzertDto.entries()))
                           .setHeader("#")
                           .setWidth("60px").setFlexGrow(0);

                    // for now, disable this column
                    if (konzertDto.hasMarschbuchEntry() && false) {
                        entries.addColumn(repertoireNumber(KonzertEntryDto::getMarschbuchNumber))
                               .setHeader("Marschbuch")
                               .setWidth("120px").setFlexGrow(0);
                    }

                    entries.addColumn(iconPopover(INFO_CIRCLE, KonzertEntryDto::getAdditionalInfo))
                           .setWidth("60px").setFlexGrow(0);

                    entries.addColumn(KonzertEntryDto::titel)
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
