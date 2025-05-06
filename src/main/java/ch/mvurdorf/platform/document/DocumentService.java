package ch.mvurdorf.platform.document;

import ch.mvurdorf.platform.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentDao documentDao;
    private final StorageService storageService;

    @Transactional
    public Long uploadDocument(CreateDocumentDto documentDto, byte[] content) {
        Document document = new Document(
            null,
            documentDto.name(),
            documentDto.fileType(),
            LocalDateTime.now(),
            documentDto.description()
        );

        documentDao.insert(document);
        storageService.write(document.getId(), content);

        return document.getId();
    }

    @Transactional(readOnly = true)
    public List<DocumentDto> getAllDocuments() {
        return documentDao.findAll().stream()
            .map(this::mapToDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<DocumentDto> getDocument(Long id) {
        try {
            return Optional.ofNullable(documentDao.findById(id))
                .map(this::mapToDto);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public Optional<byte[]> getDocumentContent(Long id) {
        if (documentDao.existsById(id)) {
            return Optional.of(storageService.read(id));
        }
        return Optional.empty();
    }

    @Transactional
    public void deleteDocument(Long id) {
        if (documentDao.existsById(id)) {
            documentDao.deleteById(id);
            // Note: This assumes the cloud storage has a way to delete blobs
            // If not, we might need to handle this differently
        }
    }

    private DocumentDto mapToDto(Document document) {
        return new DocumentDto(
            document.getId(),
            document.getName(),
            document.getFileType(),
            document.getUploadDate(),
            document.getDescription()
        );
    }
}
