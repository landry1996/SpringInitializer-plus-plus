package com.springforge.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "filesystem", matchIfMissing = true)
public class FileSystemStorageService implements StorageService {

    private final Path storageDir;

    public FileSystemStorageService(@Value("${app.storage.filesystem.base-dir:./generated}") String baseDir) {
        this.storageDir = Path.of(baseDir);
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create storage directory", e);
        }
    }

    @Override
    public String upload(Path file, String objectKey) {
        try {
            Path target = storageDir.resolve(objectKey);
            Files.createDirectories(target.getParent());
            Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
            return objectKey;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file: " + objectKey, e);
        }
    }

    @Override
    public InputStream download(String objectKey) {
        try {
            Path target = storageDir.resolve(objectKey);
            return Files.newInputStream(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file: " + objectKey, e);
        }
    }

    @Override
    public String getPresignedUrl(String objectKey) {
        return "/api/v1/generations/download/" + objectKey;
    }

    @Override
    public void delete(String objectKey) {
        try {
            Path target = storageDir.resolve(objectKey);
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete file: " + objectKey, e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        return Files.exists(storageDir.resolve(objectKey));
    }
}
