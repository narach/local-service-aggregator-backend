package com.service.sector.aggregator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link S3Service}.
 */
class S3ServiceTest {

    @Mock
    private S3Client s3;

    private S3Service service;

    private static final String BUCKET = "workplace-photos";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new S3Service(s3);
    }

    /* ------------------------------------------------------------------
     * Happy path
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("upload() sends the correct PutObjectRequest and returns the public URL")
    void upload_success() {
        // given
        byte[] bytes       = "hello".getBytes(StandardCharsets.UTF_8);
        String key         = "test/hello.txt";
        String contentType = "text/plain";

        when(s3.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().eTag("dummy").build());

        // when
        String url = service.upload(bytes, key, contentType);

        // then
        assertEquals("https://" + BUCKET + ".s3.eu-north-1.amazonaws.com/" + key, url);

        // capture & verify request details
        ArgumentCaptor<PutObjectRequest> reqCap = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> bodyCap     = ArgumentCaptor.forClass(RequestBody.class);

        verify(s3, times(1)).putObject(reqCap.capture(), bodyCap.capture());

        PutObjectRequest req = reqCap.getValue();
        assertAll(
                () -> assertEquals(BUCKET,        req.bucket()),
                () -> assertEquals(key,           req.key()),
                () -> assertEquals(contentType,   req.contentType())
        );

        // (optional) basic sanity check of the body length
        assertEquals(bytes.length, bodyCap.getValue().contentLength());
    }

    /* ------------------------------------------------------------------
     * Error propagation
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("upload() propagates S3 exceptions")
    void upload_s3Exception_propagated() {
        byte[] bytes = new byte[]{1, 2, 3};
        String key   = "err.txt";

        when(s3.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("boom").build());

        assertThrows(S3Exception.class,
                () -> service.upload(bytes, key, "application/octet-stream"));
    }
}