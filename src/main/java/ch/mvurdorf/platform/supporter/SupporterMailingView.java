package ch.mvurdorf.platform.supporter;

import ch.mvurdorf.platform.security.AuthenticatedUser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import static ch.mvurdorf.platform.security.LoginService.SUPPORTER_GROUP;
import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static ch.mvurdorf.platform.ui.ComponentUtil.secondaryButton;
import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static ch.mvurdorf.platform.ui.RendererUtil.dateTimeRenderer;
import static com.vaadin.flow.component.icon.VaadinIcon.ARROW_LEFT;
import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.EYE;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;

@PageTitle("Supporter Mailings")
@Route("supporter/mailings")
@RolesAllowed({SUPPORTER_GROUP})
public class SupporterMailingView extends VerticalLayout {

    private final SupporterMailingService supporterMailingService;
    private final AuthenticatedUser authenticatedUser;
    private final Grid<SupporterMailingDto> grid = new Grid<>();

    public SupporterMailingView(SupporterMailingService supporterMailingService,
                                AuthenticatedUser authenticatedUser) {
        this.supporterMailingService = supporterMailingService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        add(createControls(), createGrid());
        refresh();
    }

    private HorizontalLayout createControls() {
        var back = secondaryButton("Supporter", () -> UI.getCurrent().navigate(SupporterView.class));
        back.setIcon(ARROW_LEFT.create());
        var controls = new HorizontalLayout(back, new H3("Mailings"));
        controls.setAlignItems(Alignment.BASELINE);

        if (authenticatedUser.hasWritePermission(SUPPORTER_GROUP)) {
            var create = primaryButton("Neues Mailing", () -> UI.getCurrent().navigate(SupporterMailingEditView.class));
            create.setIcon(PLUS.create());
            controls.add(create);
        }
        return controls;
    }

    private Grid<SupporterMailingDto> createGrid() {
        grid.addColumn(SupporterMailingDto::subject).setHeader("Betreff").setFlexGrow(3);
        grid.addColumn(dateTimeRenderer(SupporterMailingDto::createdAt)).setHeader("Erstellt am");
        grid.addColumn(SupporterMailingDto::createdBy).setHeader("Erstellt von");
        grid.addColumn(dateTimeRenderer(SupporterMailingDto::sentAt)).setHeader("Versendet am");
        grid.addColumn(SupporterMailingDto::emailCount).setHeader("# E-Mails");
        grid.addColumn(SupporterMailingDto::postCount).setHeader("# Briefe");

        var canWrite = authenticatedUser.hasWritePermission(SUPPORTER_GROUP);
        grid.addColumn(clickableIcon(canWrite ? EDIT : EYE, this::open));
        if (canWrite) {
            grid.addColumn(clickableIcon(TRASH, this::delete, dto -> !dto.isSent(), "Löschen"));
        }
        grid.addItemDoubleClickListener(event -> open(event.getItem()));
        return grid;
    }

    private void open(SupporterMailingDto dto) {
        UI.getCurrent().navigate(SupporterMailingEditView.class, dto.id());
    }

    private void delete(SupporterMailingDto dto) {
        new ConfirmDialog("%s löschen?".formatted(dto.subject()), "Sicher?",
                          "Löschen",
                          _ -> {
                              supporterMailingService.delete(dto.id());
                              refresh();
                          },
                          "Abbrechen",
                          _ -> {
                          }).open();
    }

    private void refresh() {
        grid.setItems(supporterMailingService.findAll());
    }
}
