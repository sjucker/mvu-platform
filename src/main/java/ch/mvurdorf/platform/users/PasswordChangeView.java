package ch.mvurdorf.platform.users;

import ch.mvurdorf.platform.security.AuthenticatedUser;
import ch.mvurdorf.platform.service.FirebaseService;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER;

@PageTitle("Passwort ändern")
@Route("password-change")
@PermitAll
public class PasswordChangeView extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;
    private final FirebaseService firebaseService;

    private PasswordField currentPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;

    public PasswordChangeView(AuthenticatedUser authenticatedUser, FirebaseService firebaseService) {
        this.authenticatedUser = authenticatedUser;
        this.firebaseService = firebaseService;

        setSpacing(true);
        setPadding(true);
        createForm();
    }

    private void createForm() {
        currentPasswordField = new PasswordField("Aktuelles Passwort");
        currentPasswordField.setWidthFull();
        currentPasswordField.setRequired(true);
        currentPasswordField.setMaxWidth("400px");

        newPasswordField = new PasswordField("Neues Passwort");
        newPasswordField.setWidthFull();
        newPasswordField.setRequired(true);
        newPasswordField.setMaxWidth("400px");

        confirmPasswordField = new PasswordField("Neues Passwort bestätigen");
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setRequired(true);
        confirmPasswordField.setMaxWidth("400px");

        add(currentPasswordField, newPasswordField, confirmPasswordField,
            primaryButton("Passwort ändern", this::changePassword));
    }

    private void changePassword() {
        var currentPassword = currentPasswordField.getValue();
        var newPassword = newPasswordField.getValue();
        var confirmPassword = confirmPasswordField.getValue();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Notification.show("Bitte alle Felder ausfüllen", 3000, TOP_CENTER);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Notification.show("Die neuen Passwörter stimmen nicht überein", 3000, TOP_CENTER);
            return;
        }

        if (newPassword.length() < 6) {
            Notification.show("Das neue Passwort muss mindestens 6 Zeichen lang sein", 3000, TOP_CENTER);
            return;
        }

        if (firebaseService.changePassword(authenticatedUser.getEmail(), currentPassword, newPassword)) {
            Notification.show("Passwort erfolgreich geändert", 3000, TOP_CENTER);
            clearForm();
        } else {
            Notification.show("Passwort konnte nicht geändert werden. Bitte überprüfe dein aktuelles Passwort.", 3000, TOP_CENTER);
        }
    }

    private void clearForm() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
}
