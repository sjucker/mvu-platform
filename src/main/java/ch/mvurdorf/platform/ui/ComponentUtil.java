package ch.mvurdorf.platform.ui;

import ch.mvurdorf.platform.ui.StyleUtility.IconStyle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility.IconSize;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.function.Function;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY;
import static com.vaadin.flow.theme.lumo.LumoUtility.TextColor.PRIMARY;
import static java.util.Locale.GERMAN;

public final class ComponentUtil {

    private ComponentUtil() {
    }

    public static Icon clickableIcon(VaadinIcon vaadinIcon, Runnable action) {
        var icon = new Icon(vaadinIcon);
        icon.addClassNames(PRIMARY, IconSize.SMALL, IconStyle.CLICKABLE);
        icon.addClickListener(_ -> action.run());
        return icon;
    }

    public static Button primaryButton(String label, Runnable action) {
        var button = new Button(label, e -> action.run());
        button.addThemeVariants(LUMO_PRIMARY);
        return button;
    }

    public static DatePicker datePicker(String label) {
        var germanI18n = new DatePicker.DatePickerI18n();
        germanI18n.setMonthNames(List.of("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"));
        germanI18n.setWeekdays(List.of("Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag"));
        germanI18n.setWeekdaysShort(List.of("So", "Mo", "Di", "Mi", "Do", "Fr", "Sa"));
        germanI18n.setToday("Heute");
        germanI18n.setCancel("Abbrechen");
        germanI18n.setFirstDayOfWeek(1); // Monday

        var datePicker = new DatePicker(label);
        datePicker.setLocale(GERMAN);
        datePicker.setI18n(germanI18n);
        return datePicker;
    }

    public static <T> ValueProvider<T, Image> image(Function<T, String> name, Function<T, byte[]> bytes) {
        return t -> new Image(new StreamResource(name.apply(t),
                                                 () -> new ByteArrayInputStream(bytes.apply(t))),
                              "");
    }
}
