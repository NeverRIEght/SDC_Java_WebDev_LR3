package com.mkomarov.data;

import com.mkomarov.config.ObjectStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

public class ObjectStorageProvider {
    private static final Logger log = LoggerFactory.getLogger(ObjectStorageProvider.class);

    private static final S3Client S3_CLIENT;

    private static final Region STORAGE_REGION = Region.of(ObjectStorageProperties.REGION);

    static {
        String endpoint = ObjectStorageProperties.INTERNAL_ENDPOINT;
        String accessKey = ObjectStorageProperties.ACCESS_KEY;
        String secretKey = ObjectStorageProperties.SECRET_KEY;

        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

            S3_CLIENT = S3Client.builder()
                    .region(STORAGE_REGION)
                    .endpointOverride(new URI(endpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .forcePathStyle(true)
                    .build();

        } catch (Exception e) {
            log.error("FATAL: Failed to initialize AWS S3 Client.");
            throw new RuntimeException("S3 Client initialization failed.", e);
        }
    }

    private ObjectStorageProvider() {
    }

    public static S3Client getClient() {
        return S3_CLIENT;
    }
}
