package ch.mvurdorf.platform.users;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.Consumer;

import static ch.mvurdorf.platform.users.UserDialog.Mode.CREATE;
import static ch.mvurdorf.platform.users.UserDialog.Mode.EDIT;

public class UserDialog extends Dialog {

    private final Mode mode;
    private final Consumer<UserDto> callback;

    private EmailField email;
    private TextField name;
    private Long userId = null;

    UserDialog(Mode mode, Consumer<UserDto> callback) {
        this.mode = mode;
        this.callback = callback;
    }

    public static void create(Consumer<UserDto> callback) {
        var dialog = new UserDialog(CREATE, callback);
        dialog.init(null);
        dialog.open();
    }

    public static void edit(UserDto user, Consumer<UserDto> callback) {
        var dialog = new UserDialog(EDIT, callback);
        dialog.init(user);
        dialog.open();
    }

    private void init(UserDto user) {
        setModal(true);
        setCloseOnOutsideClick(false);

        setHeaderTitle(mode == CREATE ? "User erstellen" : "User bearbeiten");
        getFooter().add(new Button("Abbrechen", e -> close()),
                        new Button("Speichern", e -> {
                            callback.accept(new UserDto(userId,
                                                        email.getValue(),
                                                        name.getValue(),
                                                        true));
                            close();
                        }));

        var formLayout = new FormLayout();

        email = new EmailField("E-Mail");
        formLayout.add(email);

        name = new TextField("Name");
        formLayout.add(name);

        add(formLayout);

        if (user != null) {
            userId = user.id();
            email.setValue(user.email());
            name.setValue(user.name());
        }
    }

    public enum Mode {
        CREATE,
        EDIT
    }
}