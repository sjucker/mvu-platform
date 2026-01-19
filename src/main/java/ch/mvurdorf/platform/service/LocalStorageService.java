package ch.mvurdorf.platform.service;

import com.google.cloud.storage.StorageException;
import org.springframework.core.io.FileSystemResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class LocalStorageService implements StorageService {

    private final FileSystemResource root;

    public LocalStorageService(String path) {
        this.root = new FileSystemResource(path);
    }

    @Override
    public void write(String blob, byte[] content) {
        var target = getPath(blob);
        try {
            Files.copy(new ByteArrayInputStream(content), target, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException(e);
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
            throw new StorageException(e);
        }
    }

    @Override
    public boolean delete(String blob) {
        try {
            Files.delete(getPath(blob));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
