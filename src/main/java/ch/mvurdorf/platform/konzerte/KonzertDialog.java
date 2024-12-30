package ch.mvurdorf.platform.konzerte;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

import static ch.mvurdorf.platform.ui.ComponentUtil.datePicker;
import static ch.mvurdorf.platform.ui.ComponentUtil.timePicker;

@Slf4j
public class KonzertDialog extends Dialog {

    private final KonzerteService konzerteService;

    private KonzertDialog(KonzerteService konzerteService) {
        this.konzerteService = konzerteService;
    }

    public static void show(KonzerteService konzerteService, Runnable onSuccess) {
        var dialog = new KonzertDialog(konzerteService);
        dialog.init(onSuccess);
        dialog.setModal(true);
        dialog.setResizable(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setMinWidth("66%");
        dialog.open();
    }

    private void init(Runnable onSuccess) {
        setHeaderTitle("Neues Konzert");

        var content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(false);

        var formLayout = new FormLayout();
        var titel = new TextField("Titel");
        formLayout.add(titel);

        var location = new TextField("Ort");
        formLayout.add(location);

        var datum = datePicker("Datum");
        formLayout.add(datum);

        var zeit = timePicker("Zeit");
        zeit.setStep(Duration.ofMinutes(15));
        formLayout.add(zeit);

        var description = new TextField("Details");
        formLayout.add(description);
        formLayout.setColspan(description, 2);

        var entries = new Grid<KonzertEntryDto>();
        entries.setSizeFull();
        entries.addColumn(KonzertEntryDto::titel).setHeader("Titel");
        entries.setHeight("400px");

        content.add(formLayout, entries);

        add(content);
    }

}
