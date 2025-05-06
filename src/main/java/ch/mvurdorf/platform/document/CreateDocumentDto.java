package ch.mvurdorf.platform.document;

public record CreateDocumentDto(String name,
                                String fileType,
                                String description) {
}
