package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.home.NotenDownloadDialog;
import ch.mvurdorf.platform.service.StorageService;
import ch.mvurdorf.platform.utils.DateUtil;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static ch.mvurdorf.platform.ui.RendererUtil.externalLink;
import static com.vaadin.flow.component.icon.VaadinIcon.FILE_SOUND;
import static com.vaadin.flow.component.icon.VaadinIcon.MUSIC;
import static com.vaadin.flow.theme.lumo.LumoUtility.TextColor.ERROR;

@AnonymousAllowed
@PageTitle("Noten-Download")
@Route("noten-share")
public class SharedNotenView extends VerticalLayout implements HasUrlParameter<String> {

    private final ShareableLinkService shareableLinkService;
    private final NotenService notenService;
    private final StorageService storageService;

    private ShareableLinkDto link;

    public SharedNotenView(ShareableLinkService shareableLinkService, NotenService notenService, StorageService storageService) {
        this.shareableLinkService = shareableLinkService;
        this.notenService = notenService;
        this.storageService = storageService;

        setSizeFull();
    }

    private void setupView() {
        removeAll();
        add(new H3(link.description()));

        var entries = new Grid<KompositionDto>();
        entries.addColumn(clickableIcon(MUSIC,
                                        dto -> NotenDownloadDialog.show(notenService, storageService, link.instruments(), dto.id(), dto.titel())))
               .setWidth("60px").setFlexGrow(0);
        entries.addColumn(KompositionDto::titel);
        entries.addColumn(externalLink(FILE_SOUND, KompositionDto::audioSample, "Hörprobe"))
               .setWidth("60px").setFlexGrow(0);

        entries.setItems(link.kompositions());
        add(entries);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String uuid) {
        var optionalLink = shareableLinkService.findByUuid(uuid);

        if (optionalLink.isEmpty() || (optionalLink.get().validUntil().isBefore(DateUtil.today()))) {
            removeAll();
            var error = new H3("Link ungültig oder abgelaufen");
            error.addClassName(ERROR);
            add(error);
            return;
        }

        this.link = optionalLink.get();
        setupView();
    }
}
