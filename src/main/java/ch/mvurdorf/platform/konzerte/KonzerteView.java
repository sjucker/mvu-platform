package ch.mvurdorf.platform.konzerte;

import ch.mvurdorf.platform.konzerte.KonzerteService.KonzerteFilter;
import ch.mvurdorf.platform.noten.KompositionService;
import ch.mvurdorf.platform.security.AuthenticatedUser;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import static ch.mvurdorf.platform.security.LoginService.KONZERTE_GROUP;
import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static ch.mvurdorf.platform.ui.RendererUtil.dateTimeRenderer;
import static ch.mvurdorf.platform.utils.DateUtil.today;
import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.BASELINE;
import static com.vaadin.flow.data.value.ValueChangeMode.TIMEOUT;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.LIST_OL_SOLID;

@PageTitle("Konzerte")
@Route("konzerte")
@RolesAllowed({KONZERTE_GROUP})
@Menu(order = 4, icon = LIST_OL_SOLID)
public class KonzerteView extends VerticalLayout {

    private final KonzerteService konzerteService;
    private final KompositionService kompositionService;
    private final AuthenticatedUser authenticatedUser;

    private HorizontalLayout controls;
    private Grid<KonzertDto> grid;
    private ConfigurableFilterDataProvider<KonzertDto, Void, KonzerteFilter> dataProvider;

    public KonzerteView(KonzerteService konzerteService, KompositionService kompositionService, AuthenticatedUser authenticatedUser) {
        this.konzerteService = konzerteService;
        this.kompositionService = kompositionService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        createControls();
        createGrid();
        add(controls, grid);
    }

    private void createControls() {
        controls = new HorizontalLayout();
        var includingPast = new Checkbox("Vergangene anzeigen", false);
        var filter = new TextField();
        filter.setPlaceholder("Filter");
        filter.setValueChangeMode(TIMEOUT);
        filter.setClearButtonVisible(true);
        controls.add(filter, includingPast);

        filter.addValueChangeListener(event -> dataProvider.setFilter(new KonzerteFilter(event.getValue(), includingPast.getValue())));
        includingPast.addValueChangeListener(event -> dataProvider.setFilter(new KonzerteFilter(filter.getValue(), event.getValue())));

        if (authenticatedUser.hasWritePermission(KONZERTE_GROUP)) {
            var addKonzert = primaryButton("Konzert",
                                           () -> KonzertDialog.create(konzerteService,
                                                                      kompositionService,
                                                                      () -> dataProvider.refreshAll()));
            addKonzert.setIcon(PLUS.create());
            controls.add(addKonzert);
        }
        controls.setAlignItems(BASELINE);
    }

    private void createGrid() {
        grid = new Grid<>();

        if (authenticatedUser.hasWritePermission(KONZERTE_GROUP)) {
            grid.addColumn(clickableIcon(EDIT, this::edit))
                .setWidth("60px").setFlexGrow(0);

            grid.addColumn(clickableIcon(TRASH, this::delete))
                .setWidth("60px").setFlexGrow(0);
        }

        grid.addColumn(KonzertDto::name).setHeader("Name");
        grid.addColumn(dateTimeRenderer(KonzertDto::datumZeit)).setHeader("Datum / Uhrzeit");
        grid.addColumn(KonzertDto::location).setHeader("Ort");
        grid.addColumn(KonzertDto::description).setHeader("Details");

        dataProvider = konzerteService.dataProvider();
        grid.setDataProvider(dataProvider);

        if (authenticatedUser.hasWritePermission(KONZERTE_GROUP)) {
            grid.addItemDoubleClickListener(event -> edit(event.getItem()));
        }
    }

    private void delete(KonzertDto dto) {
        if (dto.datum().isBefore(today())) {
            Notification.show("Vergangene Konzerte können nicht gelöscht werden.");
        } else {
            new ConfirmDialog("%s löschen?".formatted(dto.name()), "Sicher?",
                              "Löschen",
                              _ -> {
                                  konzerteService.delete(dto);
                                  dataProvider.refreshAll();
                              },
                              "Abbrechen",
                              _ -> {
                              })
                    .open();
        }
    }

    private void edit(KonzertDto dto) {
        KonzertDialog.edit(konzerteService, kompositionService, dto, () -> dataProvider.refreshAll());
    }
}
