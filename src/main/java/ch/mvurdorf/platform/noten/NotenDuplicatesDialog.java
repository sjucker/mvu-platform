package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.service.StorageService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;
import java.util.stream.IntStream;

import static ch.mvurdorf.platform.ui.RendererUtil.clickableIcon;
import static ch.mvurdorf.platform.ui.RendererUtil.iconDownloadLink;
import static com.vaadin.flow.component.ModalityMode.STRICT;
import static com.vaadin.flow.component.Unit.PERCENTAGE;
import static com.vaadin.flow.component.icon.VaadinIcon.DOWNLOAD;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.component.notification.Notification.Position.MIDDLE;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class NotenDuplicatesDialog extends Dialog {

    private final NotenService notenService;
    private final StorageService storageService;
    private final Paragraph emptyMessage = new Paragraph("Keine Duplikate vorhanden.");
    private final Grid<DuplicateRow> grid = new Grid<>();
    private boolean deleted;

    public static void show(NotenService notenService, StorageService storageService, Consumer<Boolean> callback) {
        var dialog = new NotenDuplicatesDialog(notenService, storageService);
        dialog.init(callback);
        dialog.setModality(STRICT);
        dialog.setWidth(80, PERCENTAGE);
        dialog.open();
    }

    private void init(Consumer<Boolean> callback) {
        setHeaderTitle("Doppelte Noten");

        grid.addColumn(DuplicateRow::kompositionTitel).setHeader("Komposition").setAutoWidth(true);
        grid.addColumn(DuplicateRow::description).setHeader("Noten").setAutoWidth(true);
        grid.addColumn(DuplicateRow::version).setHeader("Upload").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(iconDownloadLink(DOWNLOAD,
                                        row -> storageService.read(row.notenPdf().id()),
                                        row -> row.notenPdf().filename()))
            .setWidth("60px")
            .setFlexGrow(0);
        grid.addColumn(clickableIcon(TRASH, this::delete, "PDF löschen")).setWidth("60px").setFlexGrow(0);
        grid.setPartNameGenerator(row -> row.groupIndex() % 2 == 0 ? null : "noten-duplicate-odd-group");

        add(emptyMessage, grid);
        refresh();

        getFooter().add(new Button("Schliessen", _ -> close()));
        addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                callback.accept(deleted);
            }
        });
    }

    private void refresh() {
        var groups = notenService.findDuplicates();
        var rows = IntStream.range(0, groups.size())
                            .boxed()
                            .flatMap(groupIndex -> {
                                var group = groups.get(groupIndex);
                                var size = group.notenPdfs().size();
                                return IntStream.range(0, size)
                                                .mapToObj(i -> new DuplicateRow(groupIndex, group, group.notenPdfs().get(i), "%d von %d".formatted(i + 1, size)));
                            })
                            .toList();
        grid.setItems(rows);
        grid.setVisible(!rows.isEmpty());
        emptyMessage.setVisible(rows.isEmpty());
    }

    private void delete(DuplicateRow row) {
        new ConfirmDialog("PDF löschen",
                          "PDF für '%s' (%s, Upload %s) definitiv löschen?".formatted(row.description(), row.kompositionTitel(), row.version()),
                          "Löschen",
                          _ -> {
                              if (!notenService.deleteNotenPdf(row.notenPdf().id())) {
                                  Notification.show("PDF konnte nicht gelöscht werden", 3000, MIDDLE);
                              }
                              deleted = true;
                              refresh();
                          },
                          "Abbrechen",
                          _ -> {
                          }).open();
    }

    private record DuplicateRow(int groupIndex, NotenDuplicateGroupDto group, NotenPdfDto notenPdf, String version) {

        String kompositionTitel() {
            return group.kompositionTitel();
        }

        String description() {
            return notenPdf.description();
        }
    }
}
