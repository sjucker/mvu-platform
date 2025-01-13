package ch.mvurdorf.platform.home;

import ch.mvurdorf.platform.konzerte.KonzertEntryDto;
import ch.mvurdorf.platform.konzerte.KonzerteService;
import ch.mvurdorf.platform.noten.NotenService;
import ch.mvurdorf.platform.service.StorageService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Whitespace;
import jakarta.annotation.security.PermitAll;

import static ch.mvurdorf.platform.konzerte.KonzerteService.getNumber;
import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static com.vaadin.flow.component.icon.VaadinIcon.MUSIC;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@PageTitle("Konzert")
@Route("konzert")
@PermitAll
public class KonzertView extends VerticalLayout implements HasUrlParameter<Long> {

    private final KonzerteService konzerteService;
    private final NotenService notenService;
    private final StorageService storageService;

    public KonzertView(KonzerteService konzerteService, NotenService notenService, StorageService storageService) {
        this.konzerteService = konzerteService;
        this.notenService = notenService;
        this.storageService = storageService;
        setSizeFull();
        create();
    }

    private void create() {
        add("");
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long konzertId) {
        konzerteService.findById(konzertId).ifPresentOrElse(
                konzertDto -> {
                    add(new H2(konzertDto.name()));
                    add(new H3(konzertDto.dateTimeAndLocation()));
                    if (isNotBlank(konzertDto.description())) {
                        var description = new Paragraph(konzertDto.description());
                        description.addClassName(Whitespace.PRE_WRAP);
                        add(description);
                    }
                    var entries = new Grid<KonzertEntryDto>();
                    entries.addColumn(dto -> getNumber(dto, konzertDto.entries()))
                           .setWidth("60px").setFlexGrow(0);
                    entries.addColumn(KonzertEntryDto::titel);
                    entries.addColumn(clickableIcon(MUSIC,
                                                    dto -> KonzertNotenDialog.show(notenService, storageService, dto.kompositionId(), dto.kompositionTitel()),
                                                    dto -> !dto.isPlaceholder())).setWidth("60px").setFlexGrow(0);
                    entries.setItems(konzertDto.entries());
                    add(entries);
                },
                () -> add(new Paragraph("Konzert nicht gefunden.")));
    }
}
