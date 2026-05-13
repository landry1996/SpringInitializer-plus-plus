package com.springforge.storage;

import java.io.InputStream;
import java.nio.file.Path;

public interface StorageService {

    String upload(Path file, String objectKey);

    InputStream download(String objectKey);

    String getPresignedUrl(String objectKey);

    void delete(String objectKey);

    boolean exists(String objectKey);
}
