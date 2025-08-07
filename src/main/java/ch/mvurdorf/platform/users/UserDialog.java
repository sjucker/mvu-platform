package ch.mvurdorf.platform.users;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Register;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.Consumer;

import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static ch.mvurdorf.platform.users.UserDialog.Mode.CREATE;
import static ch.mvurdorf.platform.users.UserDialog.Mode.EDIT;

public class UserDialog extends Dialog {

    private final Mode mode;
    private final Consumer<UserDto> callback;

    private EmailField email;
    private TextField name;
    private CheckboxGroup<Instrument> instrumentPermissions;
    private Select<Register> register;
    private Long userId = null;

    private UserDialog(Mode mode, Consumer<UserDto> callback) {
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
                        primaryButton("Speichern", () -> {
                            callback.accept(new UserDto(userId,
                                                        email.getValue(),
                                                        name.getValue(),
                                                        true,
                                                        register.getValue(),
                                                        instrumentPermissions.getValue()));
                            close();
                        }));

        var formLayout = new FormLayout();

        email = new EmailField("E-Mail");
        email.setRequired(true);
        email.setEnabled(mode == CREATE);
        formLayout.add(email);

        name = new TextField("Name");
        name.setRequired(true);
        formLayout.add(name);

        register = new Select<>();
        register.setLabel("Register");
        register.setItems(Register.values());
        register.setItemLabelGenerator(Register::getDescription);
        register.setEmptySelectionAllowed(false);
        formLayout.add(register);

        instrumentPermissions = new CheckboxGroup<>("Notenberechtigung (leer lassen, falls keine Einschränkung)", Instrument.values());
        instrumentPermissions.setItemLabelGenerator(Instrument::getDescription);
        formLayout.add(instrumentPermissions, 2);

        add(formLayout);

        if (user != null) {
            userId = user.id();
            email.setValue(user.email());
            name.setValue(user.name());
            register.setValue(user.register());
            instrumentPermissions.setValue(user.instrumentPermissions());
        }
    }

    public enum Mode {
        CREATE,
        EDIT
    }
}
