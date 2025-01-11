package ch.mvurdorf.platform.service;

public interface StorageService {

    void write(String blob, byte[] content);

    byte[] read(String blob);

    default void write(Long blob, byte[] content) {
        write(String.valueOf(blob), content);
    }

    default byte[] read(Long blob) {
        return read(String.valueOf(blob));
    }

}
