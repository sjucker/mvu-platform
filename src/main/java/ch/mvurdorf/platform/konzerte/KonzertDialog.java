package ch.mvurdorf.platform.konzerte;

import ch.mvurdorf.platform.noten.KompositionDto;
import ch.mvurdorf.platform.noten.KompositionService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

import static ch.mvurdorf.platform.ui.ComponentUtil.datePicker;
import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static ch.mvurdorf.platform.ui.ComponentUtil.secondaryButton;
import static ch.mvurdorf.platform.ui.ComponentUtil.tertiaryButton;
import static ch.mvurdorf.platform.ui.ComponentUtil.timePicker;
import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static com.vaadin.flow.component.grid.dnd.GridDropLocation.BELOW;
import static com.vaadin.flow.component.grid.dnd.GridDropMode.BETWEEN;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static org.apache.commons.lang3.StringUtils.defaultString;

@Slf4j
public class KonzertDialog extends Dialog {

    private final KonzerteService konzerteService;
    private final KompositionService kompositionService;

    private GridListDataView<KonzertEntryDto> entriesDataView;
    private KonzertEntryDto draggedItem;

    private KonzertDialog(KonzerteService konzerteService, KompositionService kompositionService) {
        this.konzerteService = konzerteService;
        this.kompositionService = kompositionService;
    }

    public static void edit(KonzerteService konzerteService, KompositionService kompositionService, KonzertDto dto, Runnable onSuccess) {
        var dialog = new KonzertDialog(konzerteService, kompositionService);
        dialog.init(dto, onSuccess);
        dialog.setModal(true);
        dialog.setResizable(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setMinWidth("66%");
        dialog.open();
    }

    public static void create(KonzerteService konzerteService, KompositionService kompositionService, Runnable onSuccess) {
        edit(konzerteService, kompositionService, KonzertDto.empty(), onSuccess);
    }

    private void init(KonzertDto dto, Runnable onSuccess) {
        setHeaderTitle("Neues Konzert");

        var content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(false);

        var formLayout = new FormLayout();
        var name = new TextField("Titel");
        name.setValue(defaultString(dto.name()));
        formLayout.add(name);

        var location = new TextField("Ort");
        location.setValue(defaultString(dto.location()));
        formLayout.add(location);

        var datum = datePicker("Datum");
        datum.setValue(dto.datum());
        formLayout.add(datum);

        var zeit = timePicker("Zeit");
        zeit.setStep(Duration.ofMinutes(15));
        zeit.setValue(dto.zeit());
        formLayout.add(zeit);

        var description = new TextArea("Details");
        description.setValue(defaultString(dto.description()));
        formLayout.add(description);
        formLayout.setColspan(description, 2);

        var kompositionSelection = new ComboBox<KompositionDto>();
        kompositionSelection.setPlaceholder("Komposition");
        kompositionSelection.setItems(kompositionService.findAllSorted());
        kompositionSelection.setItemLabelGenerator(KompositionDto::label);
        kompositionSelection.setWidthFull();

        var entriesGrid = new Grid<KonzertEntryDto>();
        entriesDataView = entriesGrid.setItems(dto.entries());
        entriesGrid.setSizeFull();
        entriesGrid.addColumn(KonzertEntryDto::titel).setHeader("Titel").setFlexGrow(1);
        entriesGrid.addColumn(clickableIcon(TRASH, item -> entriesDataView.removeItem(item))).setFlexGrow(0);
        entriesGrid.setAllRowsVisible(true);
        entriesGrid.setMinHeight("300px");
        entriesGrid.setRowsDraggable(true);

        entriesGrid.addDragStartListener(e -> {
            draggedItem = e.getDraggedItems().getFirst();
            entriesGrid.setDropMode(BETWEEN);
        });

        entriesGrid.addDropListener(e -> {
            var targetItem = e.getDropTargetItem().orElse(null);
            var dropLocation = e.getDropLocation();

            boolean droppedOntoItself = draggedItem.equals(targetItem);

            if (targetItem == null || droppedOntoItself)
                return;

            entriesDataView.removeItem(draggedItem);

            if (dropLocation == BELOW) {
                entriesDataView.addItemAfter(draggedItem, targetItem);
            } else {
                entriesDataView.addItemBefore(draggedItem, targetItem);
            }
        });

        entriesGrid.addDragEndListener(_ -> {
            draggedItem = null;
            entriesGrid.setDropMode(null);
        });

        var addButton = secondaryButton("Hinzufügen",
                                        () -> kompositionSelection.getOptionalValue()
                                                                  .ifPresent(selection -> entriesDataView.addItem(new KonzertEntryDto(null, null,
                                                                                                                                      selection.id(),
                                                                                                                                      selection.titel(),
                                                                                                                                      selection.komponist(),
                                                                                                                                      selection.arrangeur()))));

        var kompositionSelectionControls = new HorizontalLayout(kompositionSelection, addButton);
        kompositionSelectionControls.setWidthFull();

        var placeholder = new TextField();
        placeholder.setPlaceholder("Platzhalter");
        placeholder.setWidthFull();

        var addPlaceholderButton = secondaryButton("Platzhalter hinzufügen",
                                                   () -> placeholder.getOptionalValue()
                                                                    .ifPresent(value -> {
                                                                        entriesDataView.addItem(new KonzertEntryDto(null, value, null, null, null, null));
                                                                        placeholder.clear();
                                                                    }));
        var placeholderControls = new HorizontalLayout(placeholder, addPlaceholderButton);
        placeholderControls.setWidthFull();

        content.add(formLayout, kompositionSelectionControls, placeholderControls, entriesGrid);

        add(content);

        getFooter().add(tertiaryButton("Abbrechen", this::close));
        getFooter().add(primaryButton("Speichern", () -> {
            konzerteService.upsert(new KonzertDto(dto.id(),
                                                  name.getValue(),
                                                  datum.getValue(),
                                                  zeit.getValue(),
                                                  location.getValue(),
                                                  description.getValue(),
                                                  entriesDataView.getItems().toList()));
            onSuccess.run();
            close();
        }));
    }

}
