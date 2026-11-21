package ch.mvurdorf.platform.ui;

import ch.mvurdorf.platform.security.AuthenticatedUser;
import ch.mvurdorf.platform.security.PasskeyService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import static com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.FINGERPRINT_SOLID;

@AnonymousAllowed
@JsModule("./passkey.ts")
@PageTitle("Login")
@Route(value = "login")
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    private final AuthenticatedUser authenticatedUser;

    public LoginView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

        var i18n = new LoginI18n();

        var i18nHeader = new LoginI18n.Header();
        i18nHeader.setTitle("MVU Platform");
        i18nHeader.setDescription("Melde dich mit deiner E-Mail an.");
        i18n.setHeader(i18nHeader);

        var i18nForm = new LoginI18n.Form();
        i18nForm.setTitle("Login");
        i18nForm.setUsername("E-Mail");
        i18nForm.setPassword("Passwort");
        i18nForm.setSubmit("Login");
        i18nForm.setForgotPassword("Passwort vergessen");
        i18n.setForm(i18nForm);

        var i18nErrorMessage = new LoginI18n.ErrorMessage();
        i18nErrorMessage.setTitle("E-Mail oder Passwort inkorrekt");
        i18nErrorMessage.setMessage("Stelle sicher, dass du die richtige E-Mail verwendest.");
        i18nErrorMessage.setUsername("E-Mail eingeben");
        i18nErrorMessage.setPassword("Passwort eingeben");
        i18n.setErrorMessage(i18nErrorMessage);

        setI18n(i18n);

        setForgotPasswordButtonVisible(false);
        getFooter().add(createPasskeyButton());
        setOpened(true);
    }

    private Button createPasskeyButton() {
        var button = new Button("Mit Passkey anmelden", new SvgIcon(FINGERPRINT_SOLID));
        button.setWidthFull();
        button.setVisible(false);
        // on success the client redirects to the requested page
        button.addClickListener(_ -> PasskeyService.login(this).then(_ -> {
        }, _ -> Notification.show("Anmeldung mit Passkey fehlgeschlagen", 3000, TOP_CENTER)));
        PasskeyService.isSupported(this).then(Boolean.class, button::setVisible);
        return button;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            // Already logged in
            setOpened(false);
            event.forwardTo("");
        }

        setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }
}
