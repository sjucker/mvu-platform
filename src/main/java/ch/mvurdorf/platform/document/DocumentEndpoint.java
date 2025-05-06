package ch.mvurdorf.platform.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/secured/documents")
@RequiredArgsConstructor
public class DocumentEndpoint {

    private final DocumentService documentService;

    @GetMapping
    public ResponseEntity<List<DocumentDto>> getAllDocuments(@AuthenticationPrincipal Jwt jwt) {
        log.info("GET /api/secured/documents");
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocument(@PathVariable("id") Long id, @AuthenticationPrincipal Jwt jwt) {
        log.info("GET /api/secured/documents/{}", id);
        return ResponseEntity.of(documentService.getDocument(id));
    }

    @GetMapping(value = "/{id}/content", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getDocumentContent(@PathVariable("id") Long id, @AuthenticationPrincipal Jwt jwt) {
        log.info("GET /api/secured/documents/{}/content", id);
        var documentOpt = documentService.getDocument(id);

        if (documentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var document = documentOpt.get();
        var contentOpt = documentService.getDocumentContent(id);

        if (contentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.name() + "\"")
            .contentType(MediaType.parseMediaType(document.fileType()))
            .body(contentOpt.get());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @AuthenticationPrincipal Jwt jwt) throws IOException {

        log.info("POST /api/secured/documents - Uploading document: {}", name);

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var documentDto = new CreateDocumentDto(
            name,
            file.getContentType(),
            description
        );

        var id = documentService.uploadDocument(documentDto, file.getBytes());
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable("id") Long id, @AuthenticationPrincipal Jwt jwt) {
        log.info("DELETE /api/secured/documents/{}", id);

        if (documentService.getDocument(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
