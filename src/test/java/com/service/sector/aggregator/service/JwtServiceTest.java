package com.service.sector.aggregator.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new JwtService();
        setField(service, "secret", "test-secret");
        setField(service, "expiresHours", 1L);          // 1 h validity for default tests
    }

    /* ------------------------------------------------------------------
     * Happy path
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("Generated token can be parsed back to the same user-id")
    void generateAndParse_roundTrip_success() {
        long userId = 42L;

        String token   = service.generateToken(userId);
        Long  parsedId = service.parseUserId(token);

        assertEquals(userId, parsedId);
    }

    @Test
    @DisplayName("extractUserId removes the Bearer prefix correctly")
    void extractUserId_bearerPrefix_success() {
        long userId = 99L;
        String token   = service.generateToken(userId);
        String bearer  = "Bearer " + token;

        assertEquals(userId, service.extractUserId(bearer));
    }

    /* ------------------------------------------------------------------
     * Error handling
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("parseUserId throws when the signature is invalid")
    void parseUserId_invalidSignature_throws() throws Exception {
        // Token signed with a different secret
        JwtService other = new JwtService();
        setField(other, "secret", "other-secret");
        setField(other, "expiresHours", 1L);

        String foreignToken = other.generateToken(1L);

        assertThrows(JWTVerificationException.class,
                () -> service.parseUserId(foreignToken));
    }

    @Test
    @DisplayName("parseUserId throws TokenExpiredException for an expired token")
    void parseUserId_expiredToken_throws() throws Exception {
        setField(service, "expiresHours", -1L);          // already expired
        String expiredToken = service.generateToken(5L);

        assertThrows(TokenExpiredException.class,
                () -> service.parseUserId(expiredToken));
    }

    /* ------------------------------------------------------------------
     * Reflection helper
     * ------------------------------------------------------------------ */

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = JwtService.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}