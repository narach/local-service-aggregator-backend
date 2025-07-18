package com.service.sector.aggregator.service.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

    private static final String BUCKET = "workplace-photos";
    private final S3Client s3;

    public String upload(byte[] bytes, String key, String contentType) {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(BUCKET)
                .key(key)
                .contentType(contentType)
                .build();                 // no ACL header!
        s3.putObject(req, RequestBody.fromBytes(bytes));
        return String.format("https://%s.s3.eu-north-1.amazonaws.com/%s", BUCKET, key);
    }
}
