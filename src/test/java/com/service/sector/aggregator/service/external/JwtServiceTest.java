package com.service.sector.aggregator.service.external;

import com.service.sector.aggregator.data.entity.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JwtService}.
 *
 * The class is instantiated directly – Spring context is not required.
 * The private {@code secret} field is filled via reflection so that
 * we control the signing key during the test run.
 */
class JwtServiceTest {

    /** Base-64-encoded string  ‑->  "this-is-a-test-long-secret-of-32-bytes!!" (36 bytes). */
    private static final String TEST_SECRET =
            "dGhpcy1pcy1hLXRlc3QtbG9uZy1zZWNyZXQtb2YtMzItYnl0ZXMhIQ==";

    private JwtService jwtService;

    private AppUser user;

    @BeforeEach
    void setUp() throws Exception {
        // Instantiate service & inject custom secret
        jwtService = new JwtService();
        Field secretField = JwtService.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtService, TEST_SECRET);

        // dummy user entity
        user = AppUser.builder()
                .id(42L)
                .phone("+1234567890")
                .realName("John Doe")
                .build();
    }

    /* ---------------------------------------------------------------------- *
     * generateToken() & parseUserId()                                        *
     * ---------------------------------------------------------------------- */

    @Test
    void generateToken_thenParseUserId_returnsSameId() {
        String token = jwtService.generateToken(user);

        assertNotNull(token, "Token must not be null");

        Long parsedId = jwtService.parseUserId(token);
        assertEquals(user.getId(), parsedId);
    }

    /* ---------------------------------------------------------------------- *
     * extractUserId()                                                        *
     * ---------------------------------------------------------------------- */

    @Test
    void extractUserId_fromBearerPrefixedToken_returnsId() {
        String token = jwtService.generateToken(user);
        String bearer = "Bearer " + token;

        Long id = jwtService.extractUserId(bearer);

        assertEquals(user.getId(), id);
    }

    /* ---------------------------------------------------------------------- *
     * getClaims()                                                            *
     * ---------------------------------------------------------------------- */
}