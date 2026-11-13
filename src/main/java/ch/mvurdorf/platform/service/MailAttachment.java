package ch.mvurdorf.platform.service;

public record MailAttachment(String fileName,
                             String contentType,
                             byte[] data) {
}
