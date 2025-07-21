package com.service.sector.aggregator.service.external;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.service.sector.aggregator.data.entity.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private final Long USER_ID = 42L;
    private final String PHONE = "+15550000001";

    private JwtService service;

    private AppUser user;

    @BeforeEach
    void setUp() throws Exception {
        service = new JwtService();
        setField(service, "secret", "test-secret");
        setField(service, "expiresHours", 1L);

        user = AppUser.builder()
                .id(USER_ID)
                .phone(PHONE)
                .realName("Doe Jons")
                .build();// 1 h validity for default tests
    }

    /* ------------------------------------------------------------------
     * Happy path
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("Generated token can be parsed back to the same user-id")
    void generateAndParse_roundTrip_success() {
        String token   = service.generateToken(user);
        Long  parsedId = service.parseUserId(token);

        assertEquals(USER_ID, parsedId);
    }

    @Test
    @DisplayName("extractUserId removes the Bearer prefix correctly")
    void extractUserId_bearerPrefix_success() {
        String token   = service.generateToken(user);
        String bearer  = "Bearer " + token;

        assertEquals(USER_ID, service.extractUserId(bearer));
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

        String foreignToken = other.generateToken(user);

        assertThrows(JWTVerificationException.class,
                () -> service.parseUserId(foreignToken));
    }

    @Test
    @DisplayName("parseUserId throws TokenExpiredException for an expired token")
    void parseUserId_expiredToken_throws() throws Exception {
        setField(service, "expiresHours", -1L);          // already expired
        String expiredToken = service.generateToken(user);

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