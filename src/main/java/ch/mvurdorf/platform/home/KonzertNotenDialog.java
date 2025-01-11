package ch.mvurdorf.platform.home;

import ch.mvurdorf.platform.noten.NotenDto;
import ch.mvurdorf.platform.noten.NotenService;
import ch.mvurdorf.platform.service.StorageService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import lombok.RequiredArgsConstructor;

import static ch.mvurdorf.platform.ui.RendererUtil.iconDownloadLink;
import static com.vaadin.flow.component.Unit.PIXELS;
import static com.vaadin.flow.component.icon.VaadinIcon.DOWNLOAD;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class KonzertNotenDialog extends Dialog {

    private final NotenService notenService;
    private final StorageService storageService;

    public static void show(NotenService notenService, StorageService storageService, Long kompositionId, String kompositionTitel) {
        var dialog = new KonzertNotenDialog(notenService, storageService);
        dialog.init(kompositionId, kompositionTitel);
        dialog.setModal(true);
        dialog.setWidth(500, PIXELS);
        dialog.open();
    }

    private void init(Long kompositionId, String kompositionTitel) {
        setHeaderTitle(kompositionTitel);
        var noten = notenService.findByKomposition(kompositionId);
        if (noten.isEmpty()) {
            add(new Paragraph("Keine Noten vorhanden..."));
        } else {
            var grid = new Grid<NotenDto>();
            grid.addColumn(NotenDto::description);
            grid.addColumn(iconDownloadLink(DOWNLOAD,
                                            dto -> storageService.read(dto.id()),
                                            NotenDto::filename));
            grid.setItems(noten);
            add(grid);
        }
        getFooter().add(new Button("Schliessen", _ -> close()));
    }
}
