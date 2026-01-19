package ch.mvurdorf.platform.service;

import com.google.cloud.storage.Bucket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CloudStorageService implements StorageService {

    private final Bucket bucket;

    public CloudStorageService(Bucket bucket) {
        this.bucket = bucket;
    }

    @Override
    public void write(String blob, byte[] content) {
        bucket.create(blob, content);
    }

    @Override
    public byte[] read(String blob) {
        log.info("reading blob {}", blob);
        return bucket.get(blob).getContent();
    }

    @Override
    public boolean delete(String blob) {
        log.info("deleting blob {}", blob);
        return bucket.get(blob).delete();
    }

}
