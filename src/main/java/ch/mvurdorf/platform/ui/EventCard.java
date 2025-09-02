package ch.mvurdorf.platform.ui;

import ch.mvurdorf.platform.common.AbsenzState;
import ch.mvurdorf.platform.common.LocalizedEnum;
import ch.mvurdorf.platform.events.EventAbsenzStatusDto;
import ch.mvurdorf.platform.events.EventsService;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;

import static ch.mvurdorf.platform.common.AbsenzState.INACTIVE;
import static ch.mvurdorf.platform.common.AbsenzState.NEGATIVE;
import static ch.mvurdorf.platform.common.AbsenzState.POSITIVE;
import static ch.mvurdorf.platform.common.AbsenzState.UNKNOWN;
import static org.apache.commons.lang3.StringUtils.defaultString;

public class EventCard extends Card {

    public EventCard(EventAbsenzStatusDto event, EventsService eventsService) {
        setTitle(new Div(event.title()));
        setSubtitle(new Div(event.subtitle()));
        var absenzStatus = new RadioButtonGroup<AbsenzState>();
        absenzStatus.setItems(POSITIVE, NEGATIVE, INACTIVE);
        absenzStatus.setItemLabelGenerator(LocalizedEnum::getDescription);
        if (event.status() != UNKNOWN) {
            absenzStatus.setValue(event.status());
        }
        absenzStatus.setWidthFull();

        var remark = new TextField();
        remark.setPlaceholder("Bemerkung");
        remark.setValue(defaultString(event.remark()));
        remark.setWidthFull();

        absenzStatus.addValueChangeListener(e -> eventsService.updateEventAbsenzenForUser(event.loginId(), event.eventId(), e.getValue(), remark.getValue()));
        remark.addValueChangeListener(e -> eventsService.updateEventAbsenzenForUser(event.loginId(), event.eventId(), absenzStatus.getValue(), e.getValue()));

        var verticalLayout = new VerticalLayout(absenzStatus, remark);
        verticalLayout.setPadding(false);
        verticalLayout.setSpacing(false);
        addToFooter(verticalLayout);
    }

}
