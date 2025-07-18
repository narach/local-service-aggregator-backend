package com.service.sector.aggregator.service.external;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ImageService {

    /**
     * Compresses to JPEG ≤ 1 MB, max width = 2000 px (adjust as you wish).
     */
    public byte[] compress(byte[] original) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Thumbnails.of(new java.io.ByteArrayInputStream(original))
                    .size(2000, 2000)
                    .outputFormat("jpg")
                    .outputQuality(0.9f)
                    .toOutputStream(out);
            return out.toByteArray();
        }
    }
}
