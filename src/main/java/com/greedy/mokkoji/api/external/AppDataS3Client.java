package com.greedy.mokkoji.api.external;

import com.greedy.mokkoji.config.AwsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.DeleteObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedDeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Component
public class AppDataS3Client {

    private static final Logger log = LoggerFactory.getLogger(AppDataS3Client.class);

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final String bucketName;
    private final String region;
    private final String urlFormat;

    public AppDataS3Client(@Value("${aws.app-data-s3-bucket-name}") final String bucketName, @Value("${aws.region}") String region,
                           @Value("${aws.s3-url-format}") String urlFormat, final AwsConfig awsConfig, final S3Client s3Client) {
        this.bucketName = bucketName;
        this.s3Presigner = awsConfig.getPresigner();
        this.s3Client = s3Client;
        this.region = region;
        this.urlFormat = urlFormat;
    }

    public String getPublicUrl(String fileKey) {
        if (isInvalidFileKey(fileKey)) {
            return null;
        }
        return String.format(urlFormat, bucketName, region) + fileKey;
    }

    public String getPresignedPutUrl(final String fileKey) {
        if (isInvalidFileKey(fileKey)) {
            return null;
        }

        final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        final PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putObjectRequest)
                .build();

        final PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner
                .presignPutObject(presignRequest);

        return presignedPutObjectRequest.url().toString();
    }

    public String getPresignedDeleteUrl(final String fileKey) {
        if (isInvalidFileKey(fileKey)) {
            return null;
        }

        final DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        final DeleteObjectPresignRequest presignRequest = DeleteObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .deleteObjectRequest(deleteObjectRequest)
                .build();

        final PresignedDeleteObjectRequest presignedDeleteObjectRequest = s3Presigner
                .presignDeleteObject(presignRequest);

        return presignedDeleteObjectRequest.url().toString();
    }

    public void deleteObject(final String fileKey) {
        if (isInvalidFileKey(fileKey)) {
            return;
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build());
        } catch (RuntimeException e) {
            log.warn("S3 객체 삭제 실패: key={}", fileKey, e);
        }
    }

    private boolean isInvalidFileKey(String fileKey) {
        return fileKey == null || fileKey.isBlank();
    }
}
