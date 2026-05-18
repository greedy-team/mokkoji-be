package com.greedy.mokkoji.api.external;

import com.greedy.mokkoji.config.AwsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final String region;
    private final String urlFormat;

    public AppDataS3Client(@Value("${aws.app-data-s3-bucket-name}") final String bucketName, @Value("${aws.region}") String region,
                           @Value("${aws.s3-url-format}") String urlFormat, final AwsConfig awsConfig) {
        this.bucketName = bucketName;
        this.s3Presigner = awsConfig.getPresigner();
        this.region = region;
        this.urlFormat = urlFormat;
    }

    public String getPublicUrl(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) return null;
        return String.format(urlFormat, bucketName, region) + fileKey;
    }

    public String getPresignedPutUrl(final String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
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
        if (fileKey == null || fileKey.isBlank()) {
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
}
