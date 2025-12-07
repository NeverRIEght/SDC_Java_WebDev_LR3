package com.mkomarov.service;

import com.mkomarov.data.ObjectStorageProvider;
import com.mkomarov.dto.MediafileDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ObjectStorageService {
    private static final Logger log = LoggerFactory.getLogger(ObjectStorageService.class);

    private static final String BUCKET_NAME = "mediafiles";

    private final S3Client objectStorageClient = ObjectStorageProvider.getClient();

    public ObjectStorageService() {
        ensureBucketExists();
    }

    public String uploadMediaFile(MediafileDto mediafileDto)
            throws IOException {
        String originalFilename = mediafileDto.getFilename();
        String fileExtension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFilename.substring(dotIndex);
        }

        String objectName = mediafileDto.getId() + fileExtension;

        log.info("Uploading media file to S3 bucket: {}/{}", BUCKET_NAME, objectName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(objectName)
                .contentType(mediafileDto.getContentType())
                .contentLength(mediafileDto.getFileSize())
                .metadata(java.util.Map.of(
                        "x-original-filename", originalFilename,
                        "x-owner-email", mediafileDto.getOwnerEmail()
                ))
                .build();

        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(mediafileDto.getFileData())) {
            RequestBody requestBody = RequestBody.fromInputStream(dataStream, mediafileDto.getFileSize());

            objectStorageClient.putObject(putObjectRequest, requestBody);
        } catch (IOException e) {
            throw new IOException("Failed to upload media file to object storage.", e);
        }

        return objectName;
    }

    public void deleteMediaFile(String objectName) throws IOException {
        log.info("Deleting media file from S3 bucket: {}/{}", BUCKET_NAME, objectName);

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(objectName)
                    .build();

            objectStorageClient.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted media file: {}", objectName);
        } catch (S3Exception e) {
            log.error("Failed to delete media file '{}' from bucket '{}'. Error: {}",
                    objectName, BUCKET_NAME, e.getMessage());
            throw new IOException("Failed to delete media file from object storage.", e);
        }
    }

    private void ensureBucketExists() {
        log.info("Checking existence of S3 bucket: {}", BUCKET_NAME);
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(BUCKET_NAME)
                    .build();
            objectStorageClient.headBucket(headBucketRequest);
            log.info("S3 bucket '{}' already exists.", BUCKET_NAME);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                log.warn("S3 bucket '{}' does not exist. Attempting to create it.", BUCKET_NAME);
                try {
                    CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                            .bucket(BUCKET_NAME)
                            .build();
                    objectStorageClient.createBucket(createBucketRequest);
                    log.info("S3 bucket '{}' created successfully.", BUCKET_NAME);
                } catch (BucketAlreadyOwnedByYouException alreadyOwned) {
                    log.warn("S3 bucket '{}' was created by or is already owned by this account.", BUCKET_NAME);
                } catch (S3Exception creationException) {
                    log.error("Failed to create S3 bucket '{}'. Error: {}",
                            BUCKET_NAME, creationException.getMessage());
                    throw new RuntimeException("S3 bucket creation failed.", creationException);
                }
            } else {
                log.error("Error accessing S3 bucket '{}': {}", BUCKET_NAME, e.getMessage());
                throw new RuntimeException("Error accessing S3 bucket.", e);
            }
        }
    }
}
