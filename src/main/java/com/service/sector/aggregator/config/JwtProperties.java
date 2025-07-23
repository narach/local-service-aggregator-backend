package com.service.sector.aggregator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /**
     * HMAC secret key (Base-64 encoded).
     */
    private String secret = "KzBo5txZb9hS2XVue3X1M2vU0zP5b1bzYzdVZq07JRc=";

    /**
     * How long a freshly generated access-token is valid.
     */
    private Duration ttl = Duration.ofHours(72);

    /**
     * How long before expiration the filter should proactively refresh
     * the token.
     */
    private Duration refreshThreshold = Duration.ofHours(24);
}