package ch.mvurdorf.platform.absenzen;

import ch.mvurdorf.platform.ui.ComponentUtil;
import com.vaadin.flow.component.dialog.Dialog;

import static com.vaadin.flow.component.Unit.PERCENTAGE;

public class AbsenzStatusDialog extends Dialog {

    private final AbsenzenService absenzenService;

    private AbsenzStatusDialog(AbsenzenService absenzenService) {
        this.absenzenService = absenzenService;
    }

    public static void show(AbsenzenService absenzenService, Long eventId) {
        var dialog = new AbsenzStatusDialog(absenzenService);
        dialog.init(eventId);
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setHeaderTitle("Absenzen");
        dialog.setMinWidth(66, PERCENTAGE);
        dialog.open();
    }

    private void init(Long eventId) {
        absenzenService.findAbsenzenStatusByEvent(eventId);

        // TODO

        getFooter().add(ComponentUtil.secondaryButton("Schliessen", this::close));
    }
}
