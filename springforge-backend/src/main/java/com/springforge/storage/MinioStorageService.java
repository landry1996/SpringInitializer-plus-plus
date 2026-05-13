package com.springforge.storage;

import io.minio.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "minio")
public class MinioStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioStorageService.class);

    private final MinioClient minioClient;
    private final String bucket;
    private final int presignedUrlExpiry;

    public MinioStorageService(
            @Value("${app.storage.minio.endpoint}") String endpoint,
            @Value("${app.storage.minio.access-key}") String accessKey,
            @Value("${app.storage.minio.secret-key}") String secretKey,
            @Value("${app.storage.minio.bucket:springforge-generations}") String bucket,
            @Value("${app.storage.minio.presigned-url-expiry:86400}") int presignedUrlExpiry) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.bucket = bucket;
        this.presignedUrlExpiry = presignedUrlExpiry;
    }

    @PostConstruct
    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created MinIO bucket: {}", bucket);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MinIO bucket: " + bucket, e);
        }
    }

    @Override
    public String upload(Path file, String objectKey) {
        try {
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .filename(file.toString())
                    .contentType("application/zip")
                    .build());
            log.info("Uploaded to MinIO: {}/{}", bucket, objectKey);
            return objectKey;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload to MinIO: " + objectKey, e);
        }
    }

    @Override
    public InputStream download(String objectKey) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to download from MinIO: " + objectKey, e);
        }
    }

    @Override
    public String getPresignedUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .method(Method.GET)
                    .expiry(presignedUrlExpiry, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL: " + objectKey, e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
            log.info("Deleted from MinIO: {}/{}", bucket, objectKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete from MinIO: " + objectKey, e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
