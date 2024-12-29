package ch.mvurdorf.platform.passivmitglied;

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
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import static com.vaadin.flow.theme.lumo.LumoUtility.Gap.MEDIUM;

public class PassivmitgliedDialog extends Dialog {

    private final PassivmitgliedDto dto;
    private final boolean readOnly;
    private final List<PassivmitgliedPaymentComponent> newPayments;

    private VerticalLayout newPaymentsContainer;

    private PassivmitgliedDialog(PassivmitgliedDto dto, boolean readOnly) {
        this.dto = dto;
        this.readOnly = readOnly;
        this.newPayments = new ArrayList<>();
    }

    public static void showReadOnly(PassivmitgliedDto dto) {
        show(dto, true, null);
    }

    public static void show(PassivmitgliedDto dto, Consumer<List<PassivmitgliedPaymentDto>> callback) {
        show(dto, false, callback);
    }

    private static void show(PassivmitgliedDto dto, boolean readOnly, Consumer<List<PassivmitgliedPaymentDto>> callback) {
        var dialog = new PassivmitgliedDialog(dto, readOnly);
        dialog.init(callback);
        dialog.setModal(true);
        dialog.setResizable(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setMinWidth("66%");
        dialog.open();
    }

    private void init(Consumer<List<PassivmitgliedPaymentDto>> callback) {
        setHeaderTitle(dto.fullName());

        var content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(false);

        if (!readOnly) {
            var header = new FlexLayout(new H3("Zahlungen erfassen"),
                                        clickableIcon(PLUS_CIRCLE, () -> {
                                            var c = new PassivmitgliedPaymentComponent();
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

            var component = new PassivmitgliedPaymentComponent();
            newPaymentsContainer.add(component);
            newPayments.add(component);
            content.add(newPaymentsContainer);
        }

        var grid = new Grid<PassivmitgliedPaymentDto>();
        grid.addColumn(dateRenderer(PassivmitgliedPaymentDto::datum)).setHeader("Datum");
        grid.addColumn(PassivmitgliedPaymentDto::amount).setHeader("Betrag");
        grid.addColumn(PassivmitgliedPaymentDto::bemerkung).setHeader("Bemerkung");
        grid.addColumn(dateTimeRenderer(PassivmitgliedPaymentDto::createdAt)).setHeader("Erstellt am");
        grid.addColumn(PassivmitgliedPaymentDto::createdBy).setHeader("Erstellt durch");
        grid.setEmptyStateText("Noch keine Einträge");
        grid.setItems(dto.payments());

        content.add(grid);
        add(content);

        getFooter().add(new Button("Abbrechen", _ -> close()));
        if (!readOnly) {
            getFooter().add(primaryButton("Speichern", () -> {
                callback.accept(newPayments.stream()
                                           .filter(PassivmitgliedPaymentComponent::isValid)
                                           .map(PassivmitgliedPaymentComponent::toDto)
                                           .toList());
                close();
            }));
        }
    }

    private static class PassivmitgliedPaymentComponent extends HorizontalLayout {
        private final DatePicker datum = datePicker("Datum");
        private final BigDecimalField amount = new BigDecimalField("Betrag");
        private final TextField bemerkung = new TextField("Bemerkung");

        PassivmitgliedPaymentComponent() {
            add(datum, amount, bemerkung);
        }

        public boolean isValid() {
            return datum.getValue() != null && amount.getValue() != null;
        }

        public PassivmitgliedPaymentDto toDto() {
            return new PassivmitgliedPaymentDto(datum.getValue(), amount.getValue(), bemerkung.getValue(), null, null);
        }
    }
}
