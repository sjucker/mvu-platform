package ch.mvurdorf.platform.noten;

import ch.mvurdorf.platform.service.StorageService;
import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;

import java.io.ByteArrayInputStream;

import static ch.mvurdorf.platform.security.LoginService.NOTEN_GROUP;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.MUSIC_SOLID;

@PageTitle("PDF-Viewer")
@Route("pdf")
@RolesAllowed({NOTEN_GROUP})
public class PdfView extends VerticalLayout implements HasUrlParameter<Long> {

    private final StorageService storageService;

    public PdfView(StorageService storageService) {
        this.storageService = storageService;

        setSizeFull();
    }

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        var pdfViewer = new PdfViewer();
        pdfViewer.setSrc(new StreamResource("example.pdf", () -> new ByteArrayInputStream(storageService.read(parameter))));
        pdfViewer.setSizeFull();
        var splitLayout = new SplitLayout(new VerticalLayout(new H2("Test")), pdfViewer);
        splitLayout.setSizeFull();
        add(splitLayout);
    }
}
