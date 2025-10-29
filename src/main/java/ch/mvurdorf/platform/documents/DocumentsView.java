package ch.mvurdorf.platform.documents;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

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
        add(new UnorderedList(new ListItem(downloadAnchor("documents/mvu-statuten.pdf", "mvu-statuten.pdf", "MVU-Statuten (Stand 13. März 2025)"))));
    }

    private Component downloadAnchor(String path, String filename, String text) {
        return new Anchor(DownloadHandler.fromInputStream(_ -> new DownloadResponse(new ClassPathResource(path).getInputStream(), filename, null, -1)), DOWNLOAD, text);
    }

}
