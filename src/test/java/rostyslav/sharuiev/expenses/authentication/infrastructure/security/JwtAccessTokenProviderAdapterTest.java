package rostyslav.sharuiev.expenses.authentication.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rostyslav.sharuiev.expenses.authentication.application.port.out.AccessTokenProviderPort;
import rostyslav.sharuiev.expenses.authentication.infrastructure.config.AuthenticationProperties;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtAccessTokenProviderAdapterTest {

    private static final String JWT_ISSUER = "expenses-api";
    private static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final UUID USER_ID = UUID.randomUUID();

    private JwtAccessTokenProviderAdapter adapter;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        byte[] secretBytes = new byte[32];

        for (int i = 0; i < secretBytes.length; i++) {
            secretBytes[i] = (byte) (i + 1);
        }

        String jwtSecret = Base64.getEncoder().encodeToString(secretBytes);

        AuthenticationProperties properties = new AuthenticationProperties(
                Duration.ofHours(24),
                Duration.ofMinutes(15),
                jwtSecret,
                ACCESS_TOKEN_TTL,
                JWT_ISSUER
        );

        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

        adapter = new JwtAccessTokenProviderAdapter(properties, clock);

        signingKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(secretBytes);
    }

    @Test
    void shouldGenerateValidAccessToken() {
        AccessTokenProviderPort.AccessToken result = adapter.generate(USER_ID);

        assertNotNull(result);
        assertNotNull(result.value());
        assertFalse(result.value().isBlank());
        assertEquals(ACCESS_TOKEN_TTL.toSeconds(), result.expiresIn());
    }

    @Test
    void shouldContainExpectedClaims() {
        AccessTokenProviderPort.AccessToken result = adapter.generate(USER_ID);

        Claims claims = Jwts
                .parser()
                .verifyWith(signingKey)
                .clock(() -> Date.from(NOW))
                .build()
                .parseSignedClaims(result.value())
                .getPayload();

        assertEquals(USER_ID.toString(), claims.getSubject());
        assertEquals(JWT_ISSUER, claims.getIssuer());
        assertEquals(Date.from(NOW), claims.getIssuedAt());
        assertEquals(Date.from(NOW.plus(ACCESS_TOKEN_TTL)), claims.getExpiration());
    }


    @Test
    void shouldRejectNullUserId() {
        assertThrows(
                NullPointerException.class,
                () -> adapter.generate(null)
        );
    }
}
