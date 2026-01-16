package ch.mvurdorf.platform.konzerte;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.konzerte.KonzerteService.KonzerteFilter;
import ch.mvurdorf.platform.noten.KompositionService;
import ch.mvurdorf.platform.noten.NotenShareService;
import ch.mvurdorf.platform.security.AuthenticatedUser;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
    private final NotenShareService notenShareService;

    private HorizontalLayout controls;
    private Grid<KonzertDto> grid;
    private ConfigurableFilterDataProvider<KonzertDto, Void, KonzerteFilter> dataProvider;

    public KonzerteView(KonzerteService konzerteService,
                        KompositionService kompositionService,
                        AuthenticatedUser authenticatedUser,
                        NotenShareService notenShareService) {
        this.konzerteService = konzerteService;
        this.kompositionService = kompositionService;
        this.authenticatedUser = authenticatedUser;
        this.notenShareService = notenShareService;

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

            grid.addColumn(clickableIcon(com.vaadin.flow.component.icon.VaadinIcon.LINK, this::share))
                .setWidth("60px").setFlexGrow(0);

            grid.addColumn(clickableIcon(
                            com.vaadin.flow.component.icon.VaadinIcon.LIST,
                            this::viewLinks,
                            dto -> !notenShareService.listActiveLinksForKonzert(dto.id()).isEmpty(),
                            "Aktive Links anzeigen"))
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

    private void share(KonzertDto dto) {
        var dialog = new Dialog();
        dialog.setHeaderTitle("Noten-Link teilen: " + dto.name());

        var instrument = new ComboBox<Instrument>("Instrument");
        instrument.setItems(Instrument.values());
        instrument.setItemLabelGenerator(Enum::name);
        instrument.setWidthFull();

        var expires = new DateTimePicker("Gültig bis (optional)");
        expires.setWidthFull();

        var content = new VerticalLayout(new H3("Link-Einstellungen"), instrument, expires);
        content.setPadding(false);
        content.setSpacing(true);
        dialog.add(content);

        var cancel = new Button("Abbrechen", e -> dialog.close());
        var create = primaryButton("Link erstellen", () -> {
            if (instrument.getValue() == null) {
                Notification.show("Bitte ein Instrument auswählen.");
                return;
            }
            try {
                var token = notenShareService.createLink(dto.id(), instrument.getValue(), expires.getValue());
                dialog.removeAll();

                var link = "/noten-share/" + token;
                var linkField = new TextField("Öffentlicher Link");
                linkField.setWidthFull();
                linkField.setValue(link);
                linkField.setReadOnly(true);

                var copyBtn = new Button("Kopieren", ev -> UI.getCurrent().getPage().executeJs("navigator.clipboard.writeText($0)", linkField.getValue()));

                var done = primaryButton("Fertig", dialog::close);

                var actions = new HorizontalLayout(copyBtn, done);
                actions.setWidthFull();
                actions.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

                var doneLayout = new VerticalLayout(new H3("Link erstellt"), linkField, actions);
                doneLayout.setPadding(false);
                dialog.add(doneLayout);
            } catch (Exception ex) {
                Notification.show("Fehler beim Erstellen des Links");
            }
        });

        var footer = new HorizontalLayout(cancel, create);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        dialog.getFooter().add(footer);

        dialog.open();
    }

    private void viewLinks(KonzertDto dto) {
        var dialog = new Dialog();
        dialog.setHeaderTitle("Aktive Noten-Links: " + dto.name());

        var content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        try {
            var links = notenShareService.listActiveLinksForKonzert(dto.id());
            if (links.isEmpty()) {
                var empty = new H3("Keine aktiven Links vorhanden");
                content.add(empty);
            } else {
                links.forEach(link -> {
                    var instrumentAndExpiry = new H3(link.getInstrument().name() + (link.getExpiresAt() != null ? " (bis " + link.getExpiresAt() + ")" : ""));

                    var tokenField = new TextField("Token");
                    tokenField.setWidthFull();
                    tokenField.setValue(link.getToken().toString());
                    tokenField.setReadOnly(true);

                    var url = "/noten-share/" + link.getToken();
                    var urlField = new TextField("URL");
                    urlField.setWidthFull();
                    urlField.setValue(url);
                    urlField.setReadOnly(true);

                    var copyToken = new Button("Token kopieren", e -> UI.getCurrent().getPage().executeJs("navigator.clipboard.writeText($0)", tokenField.getValue()));
                    var copyUrl = new Button("URL kopieren", e -> UI.getCurrent().getPage().executeJs("navigator.clipboard.writeText($0)", urlField.getValue()));
                    var actions = new HorizontalLayout(copyToken, copyUrl);

                    var block = new VerticalLayout(instrumentAndExpiry, tokenField, urlField, actions);
                    block.setPadding(false);
                    block.setSpacing(false);
                    content.add(block);
                });
            }
        } catch (Exception e) {
            Notification.show("Fehler beim Laden der Links");
        }

        dialog.add(content);

        var close = primaryButton("Schliessen", dialog::close);
        var footer = new HorizontalLayout(close);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        dialog.getFooter().add(footer);

        dialog.open();
    }
}
