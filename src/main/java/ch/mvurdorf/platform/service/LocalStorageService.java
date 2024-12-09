package ch.mvurdorf.platform.service;

import org.springframework.core.io.FileSystemResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class LocalStorageService implements StorageService {

    private final FileSystemResource root;

    public LocalStorageService(String path) {
        this.root = new FileSystemResource(path);
        this.root.isWritable();
    }

    @Override
    public void write(String blob, byte[] content) {
        var target = getPath(blob);
        try {
            Files.copy(new ByteArrayInputStream(content), target, REPLACE_EXISTING);
        } catch (IOException e) {
            // TODO
        }
    }

    private Path getPath(String blob) {
        return Path.of(root.getFile().getPath(), blob);
    }

    @Override
    public byte[] read(String blob) {
        try {
            return Files.readAllBytes(getPath(blob));
        } catch (IOException e) {
            // TODO
            return new byte[0];
        }
    }
}
