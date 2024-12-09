package ch.mvurdorf.platform.service;

public interface StorageService {

    void write(String blob, byte[] content);

    byte[] read(String blob);

}
