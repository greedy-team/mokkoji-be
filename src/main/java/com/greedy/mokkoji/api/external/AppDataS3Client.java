package com.greedy.mokkoji.api.external;

import com.greedy.mokkoji.config.AwsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.time.Duration;

@Component
public class AppDataS3Client {

    private final S3Presigner s3Presigner;
    private final String bucketName;

    public AppDataS3Client(@Value("${aws.app-data-s3-bucket-name}") final String bucketName, final AwsConfig awsConfig) {
        this.bucketName = bucketName;
        this.s3Presigner = awsConfig.getPresigner();
    }

    public String getPresignedUrl(final String filename) {
        if (filename == null || filename.equals("")) {
            return null;
        }

        final GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .build();

        final GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5)) // 5분간 접근 허용
                .getObjectRequest(getObjectRequest)
                .build();

        final PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner
                .presignGetObject(getObjectPresignRequest);

        //presigned url 반환
        final String url = presignedGetObjectRequest.url().toString();

        s3Presigner.close();
        return url;
    }

    public String getPresignedPutUrl(final String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }

        final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .build();

        final PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putObjectRequest)
                .build();

        final PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner
                .presignPutObject(presignRequest);

        return presignedPutObjectRequest.url().toString();
    }

    public String getPresignedDeleteUrl(final String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }

        final DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
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
