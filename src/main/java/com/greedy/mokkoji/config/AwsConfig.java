package com.greedy.mokkoji.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsConfig {

    private static final String AWS_ACCESS_KEY_ID = "aws.accessKeyId";
    private static final String AWS_SECRET_ACCESS_KEY = "aws.secretAccessKey";

    private final String accessKey;
    private final String secretKey;
    private final String region;

    public AwsConfig(@Value("${aws.access-key}") final String accessKey,
                     @Value("${aws.secret-key}") final String secretKey,
                     @Value("${aws.region}") final String region) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
    }

    @Bean
    public SystemPropertyCredentialsProvider systemPropertyCredentialsProvider() {
        System.setProperty(AWS_ACCESS_KEY_ID, accessKey);
        System.setProperty(AWS_SECRET_ACCESS_KEY, secretKey);
        return SystemPropertyCredentialsProvider.create();
    }

    @Bean
    public Region getRegion() {
        return Region.of(region);
    }

    @Bean
    public S3Presigner getPresigner() {
        return S3Presigner.builder()
                .credentialsProvider(systemPropertyCredentialsProvider())
                .region(getRegion())
                .build();
    }
}
