package ch.mvurdorf.platform.documents;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static com.vaadin.flow.component.html.AttachmentType.DOWNLOAD;
import static org.vaadin.lineawesome.LineAwesomeIconUrl.FILE_ALT;

@Slf4j
@PageTitle("Dokumente")
@Route("dokumente")
@PermitAll
@Menu(order = 8, icon = FILE_ALT)
public class DocumentsView extends VerticalLayout {

    public DocumentsView() {
        setSizeFull();
        create();
    }

    private void create() {
        add(new UnorderedList(new ListItem(downloadAnchor("documents/mvu-statuten.pdf", "MVU-Statuten (Stand 13. März 2025)"))));
    }

    private Component downloadAnchor(String path, String text) {
        try {
            return new Anchor(DownloadHandler.forFile(new ClassPathResource(path).getFile()), DOWNLOAD, text);
        } catch (IOException e) {
            log.error("could not create anchor", e);
            return new Span("Ein Fehler ist aufgetreten...");
        }
    }

}
