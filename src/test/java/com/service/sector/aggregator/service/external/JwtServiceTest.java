package com.service.sector.aggregator.service.external;

import com.service.sector.aggregator.config.JwtProperties;
import com.service.sector.aggregator.data.entity.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JwtService}.
 *
 * Spring context is NOT started; the service is instantiated directly
 * with a hand-crafted {@link JwtProperties} instance.
 */
class JwtServiceTest {

    /** Base-64-encoded string  ‑-> "this-is-a-test-long-secret-of-32-bytes!!" */
    private static final String TEST_SECRET =
            "dGhpcy1pcy1hLXRlc3QtbG9uZy1zZWNyZXQtb2YtMzItYnl0ZXMhIQ==";

    private JwtService jwtService;
    private AppUser    user;

    @BeforeEach
    void setUp() {
        // Prepare properties object
        JwtProperties props = new JwtProperties();
        props.setSecret(TEST_SECRET);
        props.setTtl(Duration.ofHours(1));          // short TTL speeds up tests
        props.setRefreshThreshold(Duration.ofMinutes(10));

        jwtService = new JwtService(props);

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
        String token  = jwtService.generateToken(user);
        String bearer = "Bearer " + token;

        Long id = jwtService.extractUserId(bearer);

        assertEquals(user.getId(), id);
    }

    /* ---------------------------------------------------------------------- *
     * getClaims()                                                            *
     * ---------------------------------------------------------------------- */
}