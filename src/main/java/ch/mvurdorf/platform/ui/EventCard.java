package ch.mvurdorf.platform.ui;

import ch.mvurdorf.platform.common.AbsenzState;
import ch.mvurdorf.platform.common.LocalizedEnum;
import ch.mvurdorf.platform.events.EventAbsenzStatusDto;
import ch.mvurdorf.platform.events.EventsService;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;

import static ch.mvurdorf.platform.common.AbsenzState.INACTIVE;
import static ch.mvurdorf.platform.common.AbsenzState.NEGATIVE;
import static ch.mvurdorf.platform.common.AbsenzState.POSITIVE;
import static ch.mvurdorf.platform.common.AbsenzState.UNKNOWN;
import static com.vaadin.flow.component.icon.VaadinIcon.INFO_CIRCLE;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class EventCard extends Card {

    public EventCard(EventAbsenzStatusDto event, EventsService eventsService) {
        setTitle(new Div(event.title()));
        if (isNotBlank(event.interna())) {
            setSubtitle(subtitleWithInfo(event));
        } else {
            setSubtitle(new Div(event.subtitle()));
        }

        if (!event.infoOnly()) {
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

    private static FlexLayout subtitleWithInfo(EventAbsenzStatusDto event) {
        var icon = INFO_CIRCLE.create();
        var popover = new Popover();
        popover.setTarget(icon);
        popover.setWidth("300px");
        popover.add(new Div(event.interna()));

        var flexLayout = new FlexLayout(new Div(event.subtitle()), icon);
        flexLayout.addClassNames(FlexDirection.ROW, AlignItems.CENTER, Gap.MEDIUM);

        return flexLayout;
    }

}
