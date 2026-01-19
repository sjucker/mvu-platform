package ch.mvurdorf.platform.service;

public interface StorageService {

    void write(String blob, byte[] content);

    byte[] read(String blob);

    boolean delete(String blob);

    default void write(Long blob, byte[] content) {
        write(String.valueOf(blob), content);
    }

    default byte[] read(Long blob) {
        return read(String.valueOf(blob));
    }

    default boolean delete(Long blob) {
        return delete(String.valueOf(blob));
    }

}
