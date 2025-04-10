package ch.mvurdorf.platform.repertoire;

import ch.mvurdorf.platform.noten.KompositionDto;
import ch.mvurdorf.platform.noten.KompositionService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.BigDecimalField;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static ch.mvurdorf.platform.ui.ComponentUtil.secondaryButton;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class RepertoireEntryAddDialog extends Dialog {

    private final KompositionService kompositionService;

    public static void show(KompositionService kompositionService, Consumer<RepertoireEntryDto> callback) {
        var dialog = new RepertoireEntryAddDialog(kompositionService);
        dialog.init(callback);
        dialog.open();
    }

    private void init(Consumer<RepertoireEntryDto> callback) {
        setHeaderTitle("Komposition zu Repertoire hinzufügen");

        var kompositionen = new ComboBox<KompositionDto>();
        kompositionen.setItems(kompositionService.findAllSorted());
        kompositionen.setItemLabelGenerator(KompositionDto::label);
        kompositionen.setWidthFull();
        kompositionen.setRequired(true);
        add(kompositionen);

        var nummer = new BigDecimalField("Nummer");
        nummer.setPlaceholder("z.B. 1 oder 13.1");
        add(nummer);

        getFooter().add(secondaryButton("Abbrechen", this::close));
        getFooter().add(primaryButton("Hinzufügen", () -> {
            if (kompositionen.getOptionalValue().isEmpty()) {
                Notification.show("Bitte Komposition auswählen!");
            } else {
                var kompositionDto = kompositionen.getValue();
                callback.accept(new RepertoireEntryDto(kompositionDto.id(), kompositionDto.titel(), kompositionDto.komponist(), kompositionDto.arrangeur(), nummer.getOptionalValue().orElse(null)));
                close();
            }
        }));
    }

}
