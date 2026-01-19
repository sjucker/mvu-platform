package ch.mvurdorf.platform.home;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.noten.NotenPdfDto;
import ch.mvurdorf.platform.noten.NotenService;
import ch.mvurdorf.platform.service.StorageService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Set;

import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static ch.mvurdorf.platform.ui.RendererUtil.iconDownloadLink;
import static com.vaadin.flow.component.Unit.PIXELS;
import static com.vaadin.flow.component.icon.VaadinIcon.DOWNLOAD;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.component.notification.Notification.Position.MIDDLE;
import static java.util.stream.Collectors.toCollection;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class NotenDownloadDialog extends Dialog {

    private final NotenService notenService;
    private final StorageService storageService;

    public static void show(NotenService notenService, StorageService storageService, Set<Instrument> instrumentPermissions, Long kompositionId, String kompositionTitel) {
        show(notenService, storageService, instrumentPermissions, kompositionId, kompositionTitel, false);
    }

    public static void show(NotenService notenService, StorageService storageService, Set<Instrument> instrumentPermissions, Long kompositionId, String kompositionTitel,
                            boolean allowDeletion) {
        var dialog = new NotenDownloadDialog(notenService, storageService);
        dialog.init(kompositionId, kompositionTitel, instrumentPermissions, allowDeletion);
        dialog.setModal(true);
        dialog.setWidth(500, PIXELS);
        dialog.open();
    }

    private void init(Long kompositionId, String kompositionTitel, Set<Instrument> instrumentPermissions, boolean allowDeletion) {
        setHeaderTitle(kompositionTitel);
        var noten = notenService.findByKomposition(kompositionId)
                                .stream()
                                .filter(dto -> instrumentPermissions.isEmpty() || instrumentPermissions.stream().anyMatch(dto::allowed))
                                .collect(toCollection(ArrayList::new));

        if (noten.isEmpty()) {
            add(new Paragraph("Keine Noten vorhanden..."));
        } else {
            var grid = new Grid<NotenPdfDto>();
            var dataView = grid.setItems(noten);

            grid.addColumn(NotenPdfDto::description);
            grid.addColumn(iconDownloadLink(DOWNLOAD,
                                            dto -> storageService.read(dto.id()),
                                            NotenPdfDto::filename))
                .setWidth("60px")
                .setFlexGrow(0);

            if (allowDeletion) {
                grid.addColumn(clickableIcon(TRASH, dto -> {
                    new ConfirmDialog("PDF löschen", "PDF für '%s' definitiv löschen?".formatted(dto.description()),
                                      "Löschen",
                                      _ -> {
                                          if (notenService.deleteNotenPdf(dto.id())) {
                                              dataView.removeItem(dto);
                                          } else {
                                              Notification.show("PDF konnte nicht gelöscht werden", 3000, MIDDLE);
                                          }
                                      },
                                      "Abbrechen",
                                      _ -> {}).open();
                }, "PDF löschen")).setWidth("60px").setFlexGrow(0);
            }
            add(grid);
        }
        getFooter().add(new Button("Schliessen", _ -> close()));
    }
}
