package ch.mvurdorf.platform.events;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import java.time.Duration;
import java.util.function.Consumer;

import static ch.mvurdorf.platform.ui.ComponentUtil.datePicker;
import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static ch.mvurdorf.platform.ui.ComponentUtil.timePicker;
import static com.vaadin.flow.component.Unit.PERCENTAGE;
import static com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER;

public class EventDialog extends Dialog {

    private final Consumer<EventDataDto> callback;
    private final Binder<EventDataDto> binder = new Binder<>(EventDataDto.class);

    private EventDialog(Consumer<EventDataDto> callback) {
        this.callback = callback;
    }

    public static void show(EventDataDto existingEvent, Consumer<EventDataDto> callback) {
        var dialog = new EventDialog(callback);
        dialog.init(existingEvent);
        dialog.setModal(true);
        dialog.setMinWidth(66, PERCENTAGE);
        dialog.open();
    }

    public static void show(Consumer<EventDataDto> callback) {
        show(EventDataDto.newEvent(), callback);
    }

    private void init(EventDataDto event) {
        setModal(true);
        setCloseOnOutsideClick(false);
        setHeaderTitle("Event");

        var formLayout = new FormLayout();

        if (event.getId() != null) {
            var trackChanges = new Checkbox("Änderungen nachvollziehen");
            binder.forField(trackChanges).bind(EventDataDto::isTrackChanges, EventDataDto::setTrackChanges);
            formLayout.add(trackChanges, 2);
        }

        var titel = new TextField("Titel");
        binder.forField(titel).asRequired().bind(EventDataDto::getTitle, EventDataDto::setTitle);
        formLayout.add(titel);

        var location = new TextField("Räumlichkeiten");
        binder.forField(location).asRequired().bind(EventDataDto::getLocation, EventDataDto::setLocation);
        formLayout.add(location);

        var fromDate = datePicker("Datum von");
        binder.forField(fromDate).asRequired().bind(EventDataDto::getFromDate, EventDataDto::setFromDate);
        formLayout.add(fromDate);

        var fromTime = timePicker("Uhrzeit von");
        fromTime.setStep(Duration.ofMinutes(15));
        binder.forField(fromTime).bind(EventDataDto::getFromTime, EventDataDto::setFromTime);
        formLayout.add(fromTime);

        var toDate = datePicker("Datum bis");
        binder.forField(toDate).bind(EventDataDto::getToDate, EventDataDto::setToDate);
        formLayout.add(toDate);

        var toTime = timePicker("Uhrzeit bis");
        toTime.setStep(Duration.ofMinutes(15));
        binder.forField(toTime).bind(EventDataDto::getToTime, EventDataDto::setToTime);
        formLayout.add(toTime);

        var approximately = new Checkbox("Zeit ca.");
        binder.forField(approximately).bind(EventDataDto::isApproximately, EventDataDto::setApproximately);
        formLayout.add(approximately, 2);

        var description = new TextField("Beschreibung");
        binder.forField(description).bind(EventDataDto::getDescription, EventDataDto::setDescription);
        formLayout.add(description);

        var interna = new TextField("Interne Informationen für Musiker (wird bei den Absenzen angezeigt)");
        binder.forField(interna).bind(EventDataDto::getInterna, EventDataDto::setInterna);
        formLayout.add(interna);

        var literature = new TextField("Literatur");
        binder.forField(literature).bind(EventDataDto::getLiterature, EventDataDto::setLiterature);
        formLayout.add(literature);

        var type = new Select<EventType>();
        type.setLabel("Typ/Farbe");
        type.setItems(EventType.values());
        type.setItemLabelGenerator(EventType::getDescription);
        binder.forField(type).asRequired().bind(EventDataDto::getType, EventDataDto::setType);
        formLayout.add(type);

        var relevantForAbsenz = new Checkbox("In Absenzen", true);
        binder.forField(relevantForAbsenz).bind(EventDataDto::isRelevantForAbsenz, EventDataDto::setRelevantForAbsenz);
        formLayout.add(relevantForAbsenz, 2);

        var relevantForWebsite = new Checkbox("Auf Website darstellen", false);
        binder.forField(relevantForWebsite).bind(EventDataDto::isRelevantForWebsite, EventDataDto::setRelevantForWebsite);
        formLayout.add(relevantForWebsite, 2);

        // TODO nur info, nicht relevant für absenzen, e.g. Ostermontag

        binder.setBean(event);

        add(formLayout);

        getFooter().add(new Button("Abbrechen", _ -> close()),
                        primaryButton("Speichern", () -> {
                            if (binder.validate().isOk()) {
                                callback.accept(binder.getBean());
                                close();
                            } else {
                                Notification.show("Es gibt Validierungsfehler!", 3000, TOP_CENTER);
                            }
                        }));
    }

}
