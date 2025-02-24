package com.greedy.mokkoji.api.external;

import com.greedy.mokkoji.config.AwsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

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
}
