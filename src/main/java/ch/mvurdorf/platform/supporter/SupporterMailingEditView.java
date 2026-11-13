package ch.mvurdorf.platform.supporter;

import ch.mvurdorf.platform.security.AuthenticatedUser;
import ch.mvurdorf.platform.service.MailAttachment;
import ch.mvurdorf.platform.supporter.SupporterMailingService.Recipient;
import ch.mvurdorf.platform.ui.LocalizedEnumRenderer;
import ch.mvurdorf.platform.ui.i18n.UploadGermanI18N;
import ch.mvurdorf.platform.utils.DateUtil;
import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;
import com.vaadin.flow.theme.lumo.LumoUtility.Whitespace;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ch.mvurdorf.platform.security.LoginService.SUPPORTER_GROUP;
import static ch.mvurdorf.platform.supporter.SupporterMailingChannel.EMAIL;
import static ch.mvurdorf.platform.supporter.SupporterMailingChannel.POST;
import static ch.mvurdorf.platform.ui.ComponentUtil.primaryButton;
import static ch.mvurdorf.platform.ui.ComponentUtil.secondaryButton;
import static ch.mvurdorf.platform.ui.RendererUtil.dateTimeRenderer;
import static ch.mvurdorf.platform.utils.FormatUtil.formatDateTime;
import static com.vaadin.flow.component.html.AttachmentType.DOWNLOAD;
import static com.vaadin.flow.component.icon.VaadinIcon.ARROW_LEFT;
import static com.vaadin.flow.component.icon.VaadinIcon.DOWNLOAD_ALT;
import static com.vaadin.flow.component.icon.VaadinIcon.ENVELOPE;
import static com.vaadin.flow.component.icon.VaadinIcon.EYE;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@PageTitle("Supporter Mailing")
@Route("supporter/mailing")
@RolesAllowed({SUPPORTER_GROUP})
public class SupporterMailingEditView extends VerticalLayout implements HasUrlParameter<Long> {

    // larger attachments are often rejected by mail servers (the size grows by a third due to the encoding)
    private static final int MAX_ATTACHMENT_SIZE = 10 * 1024 * 1024;

    private final SupporterMailingService supporterMailingService;
    private final AuthenticatedUser authenticatedUser;

    private final MultiSelectComboBox<SupporterType> types = new MultiSelectComboBox<>("Typ");
    private final IntegerField paidWithinMonths = new IntegerField("Bezahlt in den letzten Monaten");
    private final Checkbox email = new Checkbox("Per E-Mail");
    private final Checkbox post = new Checkbox("Per Post");
    private final Span recipientsInfo = new Span();
    private final TextField subject = new TextField("Betreff");
    private final TextArea mjml = new TextArea("Inhalt (MJML)");

    private final Select<Recipient> previewRecipient = new Select<>();
    private final Div previewErrors = new Div();
    private final IFrame emailPreview = new IFrame();
    private final PdfViewer letterPreview = new PdfViewer();
    private final TabSheet preview = new TabSheet();
    private final VerticalLayout previewLayout = new VerticalLayout(previewRecipient, previewErrors, preview);

    private final HorizontalLayout attachmentInfo = new HorizontalLayout();

    private Long mailingId;
    // the attachment is only stored when the mailing is saved
    private String savedAttachmentName;
    private MailAttachment pendingAttachment;
    private boolean attachmentChanged;
    private List<Recipient> recipients = List.of();
    private String previewHtml;
    private boolean loading;

    public SupporterMailingEditView(SupporterMailingService supporterMailingService,
                                    AuthenticatedUser authenticatedUser) {
        this.supporterMailingService = supporterMailingService;
        this.authenticatedUser = authenticatedUser;
        setSizeFull();
        initFilter();
        initContent();
        initPreview();
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long id) {
        removeAll();
        var mailing = id == null ? null : supporterMailingService.find(id).orElse(null);
        if (id != null && mailing == null) {
            add(new H3("Mailing nicht gefunden"));
            return;
        }
        mailingId = id;
        previewHtml = null;
        previewLayout.setVisible(false);

        var readOnly = (mailing != null && mailing.isSent()) || !authenticatedUser.hasWritePermission(SUPPORTER_GROUP);
        var filter = mailing != null ? mailing.filter() : SupporterMailingFilter.defaultFilter();

        add(createHeader(mailing, readOnly));
        add(createFilter(filter, readOnly));
        add(createContent(mailing, readOnly));
        if (mailing != null && mailing.isSent()) {
            add(createSentRecipients(mailing));
        }
        add(previewLayout);
        updateRecipients();
        if (mailing != null && mailing.isSent()) {
            recipientsInfo.setText("Versendet: %d E-Mails, %d Briefe".formatted(mailing.emailCount(), mailing.postCount()));
        }
    }

    private HorizontalLayout createHeader(SupporterMailingDto mailing, boolean readOnly) {
        var back = secondaryButton("Mailings", () -> UI.getCurrent().navigate(SupporterMailingView.class));
        back.setIcon(ARROW_LEFT.create());
        var title = new H3(mailing == null ? "Neues Mailing" : mailing.subject());
        var header = new HorizontalLayout(back, title);
        header.setAlignItems(Alignment.BASELINE);
        header.setWidthFull();

        var showPreview = secondaryButton("Vorschau", this::renderPreview);
        showPreview.setIcon(EYE.create());
        header.add(showPreview);

        if (!readOnly) {
            header.add(primaryButton("Speichern", () -> save().ifPresent(_ -> Notification.show("Gespeichert!"))));
            var send = primaryButton("Senden", this::confirmSend);
            send.setIcon(ENVELOPE.create());
            header.add(send);
        }

        if (mailing != null && mailing.isSent()) {
            var info = new Span("Versendet am %s durch %s".formatted(formatDateTime(mailing.sentAt()), mailing.sentBy()));
            info.addClassNames(FontSize.SMALL, TextColor.SECONDARY);
            header.add(info);
            if (mailing.postCount() > 0) {
                var download = secondaryButton("Briefe herunterladen (PDF)", () -> downloadLetters(mailing));
                download.setIcon(DOWNLOAD_ALT.create());
                header.add(download);
            }
        }
        return header;
    }

    private void initFilter() {
        types.setItems(SupporterType.values());
        types.setItemLabelGenerator(SupporterType::getDescription);
        types.setPlaceholder("Alle");

        paidWithinMonths.setMin(1);
        paidWithinMonths.setClearButtonVisible(true);
        paidWithinMonths.setPlaceholder("Alle");
        paidWithinMonths.setHelperText("z.B. 24 = nur wer in den letzten 24 Monaten bezahlt hat");

        email.setTooltipText("Supporter mit E-Mail, die Kommunikation per E-Mail wünschen");
        post.setTooltipText("Supporter ohne E-Mail oder die Kommunikation per Post wünschen");

        types.addValueChangeListener(_ -> updateRecipients());
        paidWithinMonths.addValueChangeListener(_ -> updateRecipients());
        email.addValueChangeListener(_ -> updateRecipients());
        post.addValueChangeListener(_ -> updateRecipients());
    }

    private FormLayout createFilter(SupporterMailingFilter filter, boolean readOnly) {
        loading = true;
        types.setValue(filter.types());
        paidWithinMonths.setValue(filter.paidWithinMonths());
        email.setValue(filter.email());
        post.setValue(filter.post());
        loading = false;

        types.setReadOnly(readOnly);
        paidWithinMonths.setReadOnly(readOnly);
        email.setReadOnly(readOnly);
        post.setReadOnly(readOnly);

        var channels = new HorizontalLayout(email, post);
        var formLayout = new FormLayout(types, paidWithinMonths, channels, recipientsInfo);
        formLayout.setWidthFull();
        return formLayout;
    }

    private void initContent() {
        subject.setWidthFull();
        subject.setMaxLength(255);
        subject.setRequired(true);

        mjml.setWidthFull();
        mjml.setMinHeight("300px");
        mjml.getStyle().set("font-family", "monospace");
        mjml.setHelperText("MJML-Elemente innerhalb von <mj-body> (z.B. <mj-section>), Briefkopf, Grussformel mit Unterschriften, Link zum Supporter-Portal und Fusszeile werden automatisch ergänzt. "
                           + "Platzhalter: %s, %s, %s, %s".formatted(SupporterMailingTemplate.ANREDE, SupporterMailingTemplate.VORNAME,
                                                                     SupporterMailingTemplate.NACHNAME, SupporterMailingTemplate.PORTAL_LINK));
    }

    private VerticalLayout createContent(SupporterMailingDto mailing, boolean readOnly) {
        subject.setValue(mailing != null ? mailing.subject() : "");
        subject.setReadOnly(readOnly);
        mjml.setValue(mailing != null ? mailing.mjml() : SupporterMailingTemplate.EXAMPLE_CONTENT);
        mjml.setReadOnly(readOnly);

        var content = new VerticalLayout(subject, mjml, createAttachment(mailing, readOnly));
        content.setPadding(false);
        return content;
    }

    private VerticalLayout createAttachment(SupporterMailingDto mailing, boolean readOnly) {
        savedAttachmentName = mailing != null ? mailing.attachmentName() : null;
        pendingAttachment = null;
        attachmentChanged = false;

        var label = new Span("Anhang (nur E-Mail)");
        label.addClassNames(FontSize.SMALL, TextColor.SECONDARY);
        var attachment = new VerticalLayout(label, attachmentInfo);
        attachment.setPadding(false);
        attachment.setSpacing(false);

        if (!readOnly) {
            var upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
                pendingAttachment = new MailAttachment(metadata.fileName(),
                                                       isBlank(metadata.contentType()) ? "application/octet-stream" : metadata.contentType(),
                                                       data);
                attachmentChanged = true;
                updateAttachmentInfo(false);
            }));
            upload.setI18n(new UploadGermanI18N());
            upload.setMaxFiles(1);
            upload.setMaxFileSize(MAX_ATTACHMENT_SIZE);
            upload.addFileRejectedListener(event -> Notification.show(event.getErrorMessage()));
            upload.addAllFinishedListener(_ -> upload.clearFileList());
            attachment.add(upload);
        }
        updateAttachmentInfo(readOnly);
        return attachment;
    }

    private void updateAttachmentInfo(boolean readOnly) {
        attachmentInfo.removeAll();
        var name = attachmentChanged ? (pendingAttachment != null ? pendingAttachment.fileName() : null) : savedAttachmentName;
        if (name == null) {
            attachmentInfo.add(new Span("Kein Anhang"));
            return;
        }

        var pending = attachmentChanged ? pendingAttachment : null;
        var id = mailingId;
        attachmentInfo.add(new Anchor(DownloadHandler.fromInputStream(_ -> {
            var file = pending != null ? pending : supporterMailingService.attachment(id).orElseThrow();
            return new DownloadResponse(new ByteArrayInputStream(file.data()), file.fileName(), file.contentType(), file.data().length);
        }), DOWNLOAD, name));
        if (!readOnly) {
            var remove = new Button(TRASH.create(), _ -> {
                pendingAttachment = null;
                attachmentChanged = true;
                updateAttachmentInfo(false);
            });
            remove.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
            remove.setTooltipText("Anhang entfernen");
            attachmentInfo.add(remove);
        }
    }

    private Grid<SupporterMailingRecipientDto> createSentRecipients(SupporterMailingDto mailing) {
        var grid = new Grid<SupporterMailingRecipientDto>();
        grid.addColumn(SupporterMailingRecipientDto::name).setHeader("Name");
        grid.addColumn(SupporterMailingRecipientDto::email).setHeader("E-Mail");
        grid.addColumn(new LocalizedEnumRenderer<>(SupporterMailingRecipientDto::channel)).setHeader("Kanal");
        grid.addColumn(dateTimeRenderer(SupporterMailingRecipientDto::sentAt)).setHeader("Versendet am");
        grid.addColumn(SupporterMailingRecipientDto::error).setHeader("Fehler");
        grid.setItems(supporterMailingService.sentRecipients(mailing.id()));
        grid.setHeight("300px");
        return grid;
    }

    private void initPreview() {
        previewRecipient.setLabel("Vorschau für");
        previewRecipient.setItemLabelGenerator(recipient -> "%s (%s)".formatted(recipient.supporter().fullName(),
                                                                                recipient.channels().stream()
                                                                                         .map(SupporterMailingChannel::getDescription)
                                                                                         .collect(Collectors.joining(", "))));
        previewRecipient.addValueChangeListener(_ -> updatePreview());
        previewErrors.addClassNames(TextColor.ERROR, FontSize.SMALL, Whitespace.PRE_LINE);

        // the preview must not run any scripts
        emailPreview.setSandbox();
        emailPreview.setWidthFull();
        emailPreview.setHeight("600px");
        emailPreview.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        letterPreview.setWidthFull();
        letterPreview.setHeight("800px");
        letterPreview.setAddDownloadButton(false);

        preview.setWidthFull();
        preview.add("E-Mail", emailPreview);
        preview.add("Brief", letterPreview);

        previewLayout.setPadding(false);
    }

    private SupporterMailingFilter currentFilter() {
        return new SupporterMailingFilter(types.getValue(), paidWithinMonths.getValue(), email.getValue(), post.getValue());
    }

    private void updateRecipients() {
        if (loading) {
            return;
        }
        recipients = supporterMailingService.recipients(currentFilter());
        var emails = recipients.stream().filter(recipient -> recipient.channels().contains(EMAIL)).count();
        var letters = recipients.stream().filter(recipient -> recipient.channels().contains(POST)).count();
        recipientsInfo.setText("%d Supporter: %d E-Mails, %d Briefe".formatted(recipients.size(), emails, letters));

        var selected = previewRecipient.getValue();
        previewRecipient.setItems(recipients);
        recipients.stream()
                  .filter(recipient -> selected != null && recipient.supporter().id().equals(selected.supporter().id()))
                  .findFirst()
                  .or(() -> recipients.stream().findFirst())
                  .ifPresent(previewRecipient::setValue);
    }

    private void renderPreview() {
        if (recipients.isEmpty()) {
            Notification.show("Keine Supporter für die Vorschau gefunden");
            return;
        }
        try {
            var result = supporterMailingService.render(mjml.getValue());
            previewHtml = result.html();
            previewErrors.setText(String.join("\n", result.errors()));
            previewLayout.setVisible(true);
            updatePreview();
        } catch (RuntimeException e) {
            log.error("could not render mailing preview", e);
            Notification.show("Die Vorschau konnte nicht erstellt werden.");
        }
    }

    private void updatePreview() {
        var recipient = previewRecipient.getValue();
        if (previewHtml == null || recipient == null) {
            return;
        }
        emailPreview.setSrcdoc(supporterMailingService.emailPreview(previewHtml, recipient.supporter()));
        var pdf = supporterMailingService.letterPreview(previewHtml, recipient.supporter());
        letterPreview.setSrc(DownloadHandler.fromInputStream(_ -> new DownloadResponse(new ByteArrayInputStream(pdf),
                                                                                         "brief.pdf", "application/pdf", pdf.length)));
    }

    private Optional<Long> save() {
        if (isBlank(subject.getValue()) || isBlank(mjml.getValue())) {
            Notification.show("Bitte Betreff und Inhalt ausfüllen");
            return Optional.empty();
        }
        var isNew = mailingId == null;
        mailingId = supporterMailingService.save(mailingId, subject.getValue(), mjml.getValue(), currentFilter(), authenticatedUser.getName());
        if (attachmentChanged) {
            supporterMailingService.updateAttachment(mailingId, pendingAttachment);
            savedAttachmentName = pendingAttachment != null ? pendingAttachment.fileName() : null;
            pendingAttachment = null;
            attachmentChanged = false;
        }
        if (isNew) {
            UI.getCurrent().getPage().getHistory().replaceState(null, "supporter/mailing/%d".formatted(mailingId));
        }
        return Optional.of(mailingId);
    }

    private void confirmSend() {
        save().ifPresent(id -> {
            var emails = recipients.stream().filter(recipient -> recipient.channels().contains(EMAIL)).count();
            var letters = recipients.stream().filter(recipient -> recipient.channels().contains(POST)).count();
            new ConfirmDialog("Mailing senden?",
                              "Es werden %d E-Mails versendet und %d Briefe erstellt. Das Mailing kann danach nicht mehr bearbeitet werden.".formatted(emails, letters),
                              "Senden",
                              _ -> send(id),
                              "Abbrechen",
                              _ -> {
                              }).open();
        });
    }

    private void send(Long id) {
        try {
            supporterMailingService.send(id, authenticatedUser.getName());
            Notification.show("Das Mailing wird versendet.");
            UI.getCurrent().getPage().reload();
        } catch (RuntimeException e) {
            log.error("could not send mailing %s".formatted(id), e);
            Notification.show("Das Mailing konnte nicht versendet werden.");
        }
    }

    private void downloadLetters(SupporterMailingDto mailing) {
        var pdf = supporterMailingService.letters(mailing.id());
        var resource = DownloadHandler.fromInputStream(_ -> new DownloadResponse(new ByteArrayInputStream(pdf),
                                                                                 "briefe-%s.pdf".formatted(DateUtil.today()),
                                                                                 "application/pdf",
                                                                                 pdf.length));
        var registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().open(registration.getResourceUri().toString());
    }
}
