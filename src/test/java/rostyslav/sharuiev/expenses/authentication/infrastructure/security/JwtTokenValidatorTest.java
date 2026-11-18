package rostyslav.sharuiev.expenses.authentication.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rostyslav.sharuiev.expenses.authentication.infrastructure.config.AuthenticationProperties;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenValidatorTest {

    private static final String JWT_ISSUER = "expenses-api";
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final UUID USER_ID = UUID.randomUUID();

    private JwtTokenValidator validator;
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
                Duration.ofMinutes(15),
                JWT_ISSUER
        );

        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

        validator = new JwtTokenValidator(properties, clock);
        signingKey = Keys.hmacShaKeyFor(secretBytes);
    }

    @Test
    void shouldReturnUserIdForValidToken() {
        String token = generateToken(
                USER_ID,
                signingKey,
                JWT_ISSUER,
                NOW.plus(Duration.ofMinutes(15))
        );

        Optional<UUID> result = validator.validateAndGetUserId(token);

        assertEquals(Optional.of(USER_ID), result);
    }

    @Test
    void shouldReturnEmptyForExpiredToken() {
        String token = generateToken(
                USER_ID,
                signingKey,
                JWT_ISSUER,
                NOW.minusSeconds(1)
        );

        assertTrue(validator.validateAndGetUserId(token).isEmpty());
    }

    @Test
    void shouldReturnEmptyForInvalidSignature() {
        byte[] anotherSecret = new byte[32];

        for (int i = 0; i < anotherSecret.length; i++) {
            anotherSecret[i] = (byte) (i + 2);
        }

        SecretKey anotherKey = Keys.hmacShaKeyFor(anotherSecret);

        String token = generateToken(
                USER_ID,
                anotherKey,
                JWT_ISSUER,
                NOW.plus(Duration.ofMinutes(15))
        );

        assertTrue(validator.validateAndGetUserId(token).isEmpty());
    }

    @Test
    void shouldReturnEmptyForInvalidIssuer() {
        String token = generateToken(
                USER_ID,
                signingKey,
                "another-issuer",
                NOW.plus(Duration.ofMinutes(15))
        );

        assertTrue(validator.validateAndGetUserId(token).isEmpty());
    }

    @Test
    void shouldReturnEmptyForInvalidSubject() {
        String token = Jwts.builder()
                .subject("not-a-uuid")
                .issuer(JWT_ISSUER)
                .issuedAt(Date.from(NOW))
                .expiration(Date.from(
                        NOW.plus(Duration.ofMinutes(15))
                ))
                .signWith(signingKey)
                .compact();

        assertTrue(validator.validateAndGetUserId(token).isEmpty());
    }

    @Test
    void shouldReturnEmptyForMalformedToken() {
        assertTrue(validator.validateAndGetUserId("not-a-jwt").isEmpty());
    }

    @Test
    void shouldReturnEmptyForNullToken() {
        assertTrue(validator.validateAndGetUserId(null).isEmpty());
    }

    private String generateToken(
            UUID userId,
            SecretKey key,
            String issuer,
            Instant expiresAt
    ) {
        return Jwts
                .builder()
                .subject(userId.toString())
                .issuer(issuer)
                .issuedAt(Date.from(NOW))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }
}
