package ch.mvurdorf.platform.home;

import ch.mvurdorf.platform.common.Instrument;
import ch.mvurdorf.platform.common.Notenschluessel;
import ch.mvurdorf.platform.common.Stimme;
import ch.mvurdorf.platform.common.Stimmlage;
import ch.mvurdorf.platform.noten.NotenService;
import ch.mvurdorf.platform.repertoire.RepertoireDto;
import ch.mvurdorf.platform.security.AuthenticatedUser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;
import java.util.Set;

import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static ch.mvurdorf.platform.ui.ComponentUtil.secondaryButton;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class ExportAllPdfDialog extends Dialog {

    private final NotenService notenService;
    private final AuthenticatedUser authenticatedUser;
    private final RepertoireDto repertoire;

    public static void show(NotenService notenService, AuthenticatedUser authenticatedUser, RepertoireDto repertoire) {
        var dialog = new ExportAllPdfDialog(notenService, authenticatedUser, repertoire);
        dialog.init();
        dialog.setModal(true);
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.open();
    }

    private void init() {
        setHeaderTitle(repertoire.type().getDescription());

        var instrument = new ComboBox<>("Instrument", Instrument.values());
        instrument.setRequired(true);
        instrument.setItemLabelGenerator(Instrument::getDescription);
        if (authenticatedUser.getInstrumentPermissions().size() == 1) {
            instrument.setValue(authenticatedUser.getInstrumentPermissions().stream().findFirst().orElseThrow());
        }

        var stimme = new CheckboxGroup<>("Stimme", Stimme.values());
        stimme.setItemLabelGenerator(Stimme::getDescription);

        var stimmlage = new CheckboxGroup<>("Stimmlage", Stimmlage.values());
        stimmlage.setItemLabelGenerator(Stimmlage::getDescription);

        var noteneschluessel = new CheckboxGroup<>("Notenschlüssel", Notenschluessel.values());
        noteneschluessel.setItemLabelGenerator(Notenschluessel::getDescription);

        var formLayout = new FormLayout();
        formLayout.add(instrument, 2);
        formLayout.add(stimme, 2);
        formLayout.add(stimmlage, 2);
        formLayout.add(noteneschluessel, 2);

        var info = new Paragraph("Stimme, Stimmlage und Notenschlüssel kann leer gelassen werden.");
        info.addClassName(FontSize.XSMALL);
        add(info, formLayout);

        getFooter().add(secondaryButton("Schliessen", this::close),
                        primaryButton("Exportieren", () -> {
                            if (instrument.isEmpty()) {
                                Notification.show("Instrument auswählen!");
                            } else {
                                export(repertoire,
                                       instrument.getValue(),
                                       stimme.getValue(),
                                       stimmlage.getValue(),
                                       noteneschluessel.getValue());
                            }
                        }));
    }

    private void export(RepertoireDto repertoire, Instrument instrument, Set<Stimme> stimmen, Set<Stimmlage> stimmlagen, Set<Notenschluessel> noteneschluessel) {
        var bytes = notenService.exportNotenToPdf(repertoire.kompositionIds(), instrument, stimmen, stimmlagen, noteneschluessel);

        var resource = new StreamResource(repertoire.type().getDescription() + ".pdf", (InputStreamFactory) () -> new ByteArrayInputStream(bytes));
        var registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().open(registration.getResourceUri().toString());
    }

}
