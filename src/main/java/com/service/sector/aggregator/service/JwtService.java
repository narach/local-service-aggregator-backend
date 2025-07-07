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

    @Value("${security.jwt.expires-minutes:120}")
    private long expiresMinutes;

    public String generateToken(Long userId, String subject) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(subject)
                .withClaim("uid", userId)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(expiresMinutes * 60)))
                .sign(Algorithm.HMAC256(secret));
    }
}
