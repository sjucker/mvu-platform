package ch.mvurdorf.platform.users;

import ch.mvurdorf.platform.security.AuthenticatedUser;
import ch.mvurdorf.platform.security.PasskeyService;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.web.webauthn.api.CredentialRecord;

import static ch.mvurdorf.platform.ui.ComponentUtil.clickableIcon;
import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static ch.mvurdorf.platform.utils.FormatUtil.formatInstant;
import static com.vaadin.flow.component.grid.ColumnTextAlign.CENTER;
import static com.vaadin.flow.component.grid.GridVariant.LUMO_COMPACT;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.BASELINE;

@PageTitle("Passkeys")
@Route("passkeys")
@PermitAll
@JsModule("./passkey.ts")
public class PasskeyView extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;
    private final PasskeyService passkeyService;

    private final Grid<CredentialRecord> grid = new Grid<>();
    private final TextField labelField = new TextField("Bezeichnung");

    public PasskeyView(AuthenticatedUser authenticatedUser, PasskeyService passkeyService) {
        this.authenticatedUser = authenticatedUser;
        this.passkeyService = passkeyService;

        setSpacing(true);
        setPadding(true);
        setHeightFull();

        add(new Paragraph("Mit einem Passkey kannst du dich ohne Passwort anmelden, z.B. mit Fingerabdruck, Gesichtserkennung oder der PIN deines Geräts."));
        createForm();
        createGrid();
    }

    private void createForm() {
        labelField.setPlaceholder("z.B. iPhone");
        labelField.setRequired(true);
        labelField.setMaxLength(100);

        var addButton = primaryButton("Passkey hinzufügen", this::register);
        var form = new HorizontalLayout(labelField, addButton);
        form.setDefaultVerticalComponentAlignment(BASELINE);
        add(form);
    }

    private void createGrid() {
        grid.addThemeVariants(LUMO_COMPACT);
        grid.setMaxWidth("800px");
        grid.addColumn(CredentialRecord::getLabel).setHeader("Bezeichnung");
        grid.addColumn(passkey -> formatInstant(passkey.getCreated())).setHeader("Erstellt");
        grid.addColumn(passkey -> formatInstant(passkey.getLastUsed())).setHeader("Zuletzt verwendet");
        grid.addComponentColumn(passkey -> clickableIcon(TRASH, () -> delete(passkey)))
            .setWidth("60px").setTextAlign(CENTER).setFlexGrow(0);
        add(grid);
        refresh();
    }

    private void register() {
        var label = labelField.getValue().trim();
        if (label.isEmpty()) {
            Notification.show("Bitte eine Bezeichnung eingeben", 3000, TOP_CENTER);
            return;
        }

        PasskeyService.register(this, label).then(_ -> {
            Notification.show("Passkey hinzugefügt", 3000, TOP_CENTER);
            labelField.clear();
            refresh();
        }, _ -> Notification.show("Passkey konnte nicht hinzugefügt werden", 3000, TOP_CENTER));
    }

    private void delete(CredentialRecord passkey) {
        passkeyService.delete(authenticatedUser.getEmail(), passkey);
        Notification.show("Passkey gelöscht", 3000, TOP_CENTER);
        refresh();
    }

    private void refresh() {
        grid.setItems(passkeyService.getPasskeys(authenticatedUser.getEmail()));
    }
}
