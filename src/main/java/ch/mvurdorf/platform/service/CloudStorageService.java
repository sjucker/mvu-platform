package ch.mvurdorf.platform.service;

import com.google.cloud.storage.Bucket;

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
        return bucket.get(blob).getContent();
    }
}
