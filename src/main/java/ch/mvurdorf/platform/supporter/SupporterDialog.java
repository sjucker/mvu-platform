package ch.mvurdorf.platform.supporter;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static ch.mvurdorf.platform.ui.ComponentUtil.clickableIcon;
import static ch.mvurdorf.platform.ui.ComponentUtil.datePicker;
import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static ch.mvurdorf.platform.ui.RendererUtil.dateRenderer;
import static ch.mvurdorf.platform.ui.RendererUtil.dateTimeRenderer;
import static com.vaadin.flow.component.Unit.PERCENTAGE;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import static com.vaadin.flow.theme.lumo.LumoUtility.Gap.MEDIUM;

public class SupporterDialog extends Dialog {

    private final SupporterDto dto;
    private final boolean readOnly;
    private final List<SupporterPaymentComponent> newPayments;

    private VerticalLayout newPaymentsContainer;

    private SupporterDialog(SupporterDto dto, boolean readOnly) {
        this.dto = dto;
        this.readOnly = readOnly;
        this.newPayments = new ArrayList<>();
    }

    public static void showReadOnly(SupporterDto dto) {
        show(dto, true, null);
    }

    public static void show(SupporterDto dto, Consumer<List<SupporterPaymentDto>> callback) {
        show(dto, false, callback);
    }

    private static void show(SupporterDto dto, boolean readOnly, Consumer<List<SupporterPaymentDto>> callback) {
        var dialog = new SupporterDialog(dto, readOnly);
        dialog.init(callback);
        dialog.setModal(true);
        dialog.setResizable(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setMinWidth(66, PERCENTAGE);
        dialog.open();
    }

    private void init(Consumer<List<SupporterPaymentDto>> callback) {
        setHeaderTitle(dto.fullName());

        var content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(false);

        if (!readOnly) {
            var header = new FlexLayout(new H3("Zahlungen erfassen"),
                                        clickableIcon(PLUS_CIRCLE, () -> {
                                            var c = new SupporterPaymentComponent();
                                            newPaymentsContainer.add(c);
                                            newPayments.add(c);
                                        }));
            header.setWidthFull();
            header.addClassNames(MEDIUM);
            header.setAlignItems(CENTER);
            content.add(header);

            newPaymentsContainer = new VerticalLayout();
            newPaymentsContainer.setSpacing(false);
            newPaymentsContainer.setPadding(false);

            var component = new SupporterPaymentComponent();
            newPaymentsContainer.add(component);
            newPayments.add(component);
            content.add(newPaymentsContainer);
        }

        var grid = new Grid<SupporterPaymentDto>();
        grid.addColumn(dateRenderer(SupporterPaymentDto::datum)).setHeader("Datum");
        grid.addColumn(SupporterPaymentDto::amount).setHeader("Betrag");
        grid.addColumn(SupporterPaymentDto::bemerkung).setHeader("Bemerkung");
        grid.addColumn(dateTimeRenderer(SupporterPaymentDto::createdAt)).setHeader("Erstellt am");
        grid.addColumn(SupporterPaymentDto::createdBy).setHeader("Erstellt durch");
        grid.setEmptyStateText("Noch keine Einträge");
        grid.setItems(dto.payments());

        content.add(grid);
        add(content);

        getFooter().add(new Button("Abbrechen", e -> close()));
        if (!readOnly) {
            getFooter().add(primaryButton("Speichern", () -> {
                callback.accept(newPayments.stream()
                                           .filter(SupporterPaymentComponent::isValid)
                                           .map(SupporterPaymentComponent::toDto)
                                           .toList());
                close();
            }));
        }
    }

    private static class SupporterPaymentComponent extends HorizontalLayout {
        private final DatePicker datum = datePicker("Datum");
        private final BigDecimalField amount = new BigDecimalField("Betrag");
        private final TextField bemerkung = new TextField("Bemerkung");

        SupporterPaymentComponent() {
            add(datum, amount, bemerkung);
        }

        public boolean isValid() {
            return datum.getValue() != null && amount.getValue() != null;
        }

        public SupporterPaymentDto toDto() {
            return new SupporterPaymentDto(datum.getValue(), amount.getValue(), bemerkung.getValue(), null, null);
        }
    }
}
