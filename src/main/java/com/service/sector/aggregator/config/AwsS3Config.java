package com.service.sector.aggregator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Configures an {@link S3Client} bean for AWS SDK v2.
 * <p>
 * Credentials are resolved via the default provider chain
 * (env vars, system properties, ~/.aws/credentials, EC2/ECS metadata, etc.).
 */
@Configuration
public class AwsS3Config {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.EU_NORTH_1)
                .build();
    }
}
