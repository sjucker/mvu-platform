package ch.mvurdorf.platform.supporter;

import ch.mvurdorf.platform.security.AuthenticatedUser;
import ch.mvurdorf.platform.ui.LocalizedEnumRenderer;
import ch.mvurdorf.platform.utils.DateUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadResponse;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static ch.mvurdorf.platform.security.LoginService.SUPPORTER_GROUP;
import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static ch.mvurdorf.platform.ui.ComponentUtil.secondaryButton;
import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static ch.mvurdorf.platform.ui.RendererUtil.dateRenderer;
import static ch.mvurdorf.platform.ui.RendererUtil.dateTimeRenderer;
import static com.vaadin.flow.component.icon.VaadinIcon.DOWNLOAD_ALT;
import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.EYE;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS;
import static com.vaadin.flow.data.value.ValueChangeMode.TIMEOUT;
import static com.vaadin.flow.server.streams.DownloadHandler.fromInputStream;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.MONEY_BILL_ALT;

@Slf4j
@PageTitle("Supporter")
@Route("supporter")
@RolesAllowed({SUPPORTER_GROUP})
@Menu(order = 7, icon = MONEY_BILL_ALT)
public class SupporterView extends VerticalLayout {

    private final SupporterService supporterService;
    private final SupporterExcelService supporterExcelService;
    private final AuthenticatedUser authenticatedUser;

    private HorizontalLayout controls;
    private Grid<SupporterDto> grid;
    private ConfigurableFilterDataProvider<SupporterDto, Void, String> dataProvider;

    public SupporterView(SupporterService supporterService,
                         SupporterExcelService supporterExcelService,
                         AuthenticatedUser authenticatedUser) {
        this.supporterService = supporterService;
        this.supporterExcelService = supporterExcelService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        createControls();
        createGrid();
        add(controls, grid);
    }

    private void createControls() {
        controls = new HorizontalLayout();
        var filter = new TextField(event -> dataProvider.setFilter(event.getValue()));
        filter.setPlaceholder("Filter");
        filter.setValueChangeMode(TIMEOUT);
        filter.setClearButtonVisible(true);
        controls.add(filter);

        if (authenticatedUser.hasWritePermission(SUPPORTER_GROUP)) {
            var addVouchers = primaryButton("Gutscheine", () -> VouchersDialog.show(supporterService,
                                                                                    created -> {
                                                                                        if (created) {
                                                                                            dataProvider.refreshAll();
                                                                                        }
                                                                                    }));
            addVouchers.setIcon(PLUS.create());
            controls.add(addVouchers);
        }

        var exportExcelButton = secondaryButton("Excel Export", () -> {
            try (var inputStream = supporterExcelService.export()) {
                var resource = fromInputStream(_ -> new DownloadResponse(inputStream, "supporters-%s.xlsx".formatted(DateUtil.today()), "application/xlsx", -1));
                var registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
                UI.getCurrent().getPage().open(registration.getResourceUri().toString());
            } catch (IOException e) {
                log.error("could not export excel", e);
                Notification.show("Ein Fehler ist aufgetreten.");
            }
        });
        exportExcelButton.setIcon(DOWNLOAD_ALT.create());

        controls.add(exportExcelButton);
    }

    private void createGrid() {
        grid = new Grid<>();
        grid.addColumn(new LocalizedEnumRenderer<>(SupporterDto::type)).setHeader("Typ");
        grid.addColumn(SupporterDto::externalId).setHeader("Referenz-Nr.");
        grid.addColumn(SupporterDto::vorname).setHeader("Vorname");
        grid.addColumn(SupporterDto::nachname).setHeader("Nachname");
        grid.addColumn(SupporterDto::email).setHeader("Email");
        grid.addColumn(SupporterDto::getStrasseWithNr).setHeader("Strasse");
        grid.addColumn(SupporterDto::getPlzOrt).setHeader("PLZ/Ort");
        grid.addColumn(dateTimeRenderer(SupporterDto::registeredAt)).setHeader("Registriert am");
        grid.addColumn(SupporterDto::numberOfPayments).setHeader("# Bezahlungen");
        grid.addColumn(dateRenderer(dto -> dto.lastPayment().orElse(null))).setHeader("Letzte Bezahlung");
        if (authenticatedUser.hasWritePermission(SUPPORTER_GROUP)) {
            grid.addColumn(clickableIcon(EDIT,
                                         dto -> SupporterDialog.show(dto, newPayments -> {
                                             supporterService.addPayments(dto.id(), newPayments, authenticatedUser.getName());
                                             dataProvider.refreshAll();
                                         })));
        }
        if (authenticatedUser.hasReadPermission(SUPPORTER_GROUP)) {
            grid.addColumn(clickableIcon(EYE, SupporterDialog::showReadOnly));
        }

        dataProvider = supporterService.dataProvider();
        grid.setDataProvider(dataProvider);
    }
}
