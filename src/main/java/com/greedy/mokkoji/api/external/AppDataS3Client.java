package com.greedy.mokkoji.api.external;

import com.greedy.mokkoji.config.AwsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.DeleteObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedDeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.List;

@Component
public class AppDataS3Client {

    private static final Logger log = LoggerFactory.getLogger(AppDataS3Client.class);

    private static final int DELETE_OBJECTS_BATCH_SIZE = 1000;

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

    @Async("s3DeleteExecutor")
    public void deleteObjectsAsync(final List<String> fileKeys) {
        final List<String> validFileKeys = fileKeys.stream()
                .filter(fileKey -> !isInvalidFileKey(fileKey))
                .toList();

        for (int i = 0; i < validFileKeys.size(); i += DELETE_OBJECTS_BATCH_SIZE) {
            deleteObjects(validFileKeys.subList(i, Math.min(i + DELETE_OBJECTS_BATCH_SIZE, validFileKeys.size())));
        }
    }

    private void deleteObjects(final List<String> fileKeys) {
        final List<ObjectIdentifier> objects = fileKeys.stream()
                .map(fileKey -> ObjectIdentifier.builder().key(fileKey).build())
                .toList();

        try {
            final DeleteObjectsResponse response = s3Client.deleteObjects(DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder().objects(objects).build())
                    .build());

            response.errors().forEach(error ->
                    log.warn("S3 객체 삭제 실패: key={}, code={}, message={}", error.key(), error.code(), error.message()));
        } catch (RuntimeException e) {
            log.warn("S3 객체 일괄 삭제 요청 실패: keys={}", fileKeys, e);
        }
    }

    public void copyObject(final String sourceKey, final String destKey) {
        if (isInvalidFileKey(sourceKey) || isInvalidFileKey(destKey)) {
            return;
        }

        final CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(sourceKey)
                .destinationBucket(bucketName)
                .destinationKey(destKey)
                .build();

        s3Client.copyObject(copyObjectRequest);
    }

    private boolean isInvalidFileKey(String fileKey) {
        return fileKey == null || fileKey.isBlank();
    }
}
