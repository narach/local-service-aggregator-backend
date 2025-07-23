package com.service.sector.aggregator.service.external;

import com.service.sector.aggregator.config.JwtProperties;
import com.service.sector.aggregator.data.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

/**
 * Very lightweight JWT helper (HS256, short‑lived). No refresh‑token flow yet.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties props;

    public String generateToken(AppUser user) {
        Instant now = Instant.now();
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.getSecret()));
        String token = Jwts.builder()
                .subject(user.getId().toString())
                .claim("uid", user.getId())
                .claim("phone", user.getPhone())
                .claim("name", user.getRealName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(props.getTtl())))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return token;
    }

    public Long parseUserId(String token) {
        Jws<Claims> jws = getClaims(token);
        Claims claims = jws.getPayload();
        Long userId = Long.valueOf(claims.getSubject());
        return userId;
    }

    public Long extractUserId(String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        return parseUserId(token);
    }

    public Jws<Claims> getClaims(String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.getSecret()));
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }
}
