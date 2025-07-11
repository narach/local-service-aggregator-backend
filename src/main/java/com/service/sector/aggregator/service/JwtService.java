package com.service.sector.aggregator.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

/**
 * Very lightweight JWT helper (HS256, short‑lived). No refresh‑token flow yet.
 */
@Service
public class JwtService {

    @Value("${security.jwt.secret:dev-secret-key}")
    private String secret;

    @Value("${security.jwt.expires-hours:72}")
    private long expiresHours;

    public String generateToken(Long userId) {
        Instant now = Instant.now();
        return JWT.create()
                .withClaim("uid", userId)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(expiresHours * 3600)))
                .sign(Algorithm.HMAC256(secret));
    }

    public Long parseUserId(String token) {
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token)
                .getClaim("uid")
                .asLong();
    }

    public Long extractUserId(String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        return parseUserId(token);
    }
}
