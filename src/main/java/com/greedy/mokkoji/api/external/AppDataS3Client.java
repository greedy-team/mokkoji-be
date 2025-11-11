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
import java.util.UUID;

@Component
public class AppDataS3Client {

    private final S3Presigner s3Presigner;
    private final String bucketName;

    public AppDataS3Client(@Value("${aws.app-data-s3-bucket-name}") final String bucketName, final AwsConfig awsConfig) {
        this.bucketName = bucketName;
        this.s3Presigner = awsConfig.getPresigner();
    }

    public String getPresignedUrl(final String fileKey) {
        if (fileKey == null || fileKey.equals("")) {
            return null;
        }

        final GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        final GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15)) // 15분간 접근 허용
                .getObjectRequest(getObjectRequest)
                .build();

        final PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner
                .presignGetObject(getObjectPresignRequest);

        //presigned url 반환
        final String url = presignedGetObjectRequest.url().toString();

        return url;
    }

    public String getPresignedPutUrl(final String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return null;
        }

        String uniqueFileKey = appendUUID(fileKey);

        final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueFileKey)
                .build();

        final PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putObjectRequest)
                .build();

        final PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner
                .presignPutObject(presignRequest);

        return presignedPutObjectRequest.url().toString();
    }

    private String appendUUID(String fileKey) {
        int dotIndex = fileKey.lastIndexOf('.');
        int sliceIndex = fileKey.lastIndexOf('/');
        String uuid = UUID.randomUUID().toString();

        String prevDot = fileKey.substring(0, dotIndex);
        String nextDot = fileKey.substring(dotIndex); //jpg와 같은 확장자 의미

        if (dotIndex == (sliceIndex + 1)) {
            return prevDot + uuid + nextDot;
        }

        return prevDot + "_" + uuid + nextDot;
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
