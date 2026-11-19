package rostyslav.sharuiev.expenses.session.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import rostyslav.sharuiev.expenses.session.application.exception.RefreshTokenReuseException;
import rostyslav.sharuiev.expenses.session.application.port.out.RefreshTokenRepositoryPort;
import rostyslav.sharuiev.expenses.session.domain.model.RefreshToken;
import rostyslav.sharuiev.expenses.shared.application.port.out.TokenHasherPort;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RefreshTokenRotationIntegrationTest {

    @Autowired
    private RefreshTokenRotationService service;

    @Autowired
    private RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    @Autowired
    private TokenHasherPort tokenHasherPort;

    @Test
    void rotateRefreshToken_shouldPersistSessionRevocationWhenTokenIsReused() {
        Instant now = Instant.now();

        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String rawRefreshToken = UUID.randomUUID().toString();
        String tokenHash = tokenHasherPort.hash(rawRefreshToken);

        RefreshToken replacedToken = RefreshToken.create(
                userId,
                sessionId,
                tokenHash,
                now.plus(Duration.ofDays(30)),
                now.minus(Duration.ofDays(1))
        );

        UUID replacementTokenId = UUID.randomUUID();
        replacedToken.replaceWith(replacementTokenId, now.minusSeconds(1));

        RefreshToken activeToken = RefreshToken.create(
                userId,
                sessionId,
                UUID.randomUUID().toString(),
                now.plus(Duration.ofDays(30)),
                now.minus(Duration.ofDays(1))
        );

        refreshTokenRepositoryPort.save(replacedToken);
        refreshTokenRepositoryPort.save(activeToken);

        assertThrows(
                RefreshTokenReuseException.class,
                () -> service.rotateRefreshToken(rawRefreshToken)
        );

        List<RefreshToken> sessionTokens = refreshTokenRepositoryPort.findAllBySessionId(sessionId);

        assertEquals(2, sessionTokens.size());

        RefreshToken persistedReplacedToken = sessionTokens
                .stream()
                .filter(token -> token.getId().equals(replacedToken.getId()))
                .findFirst()
                .orElseThrow();

        RefreshToken persistedActiveToken = sessionTokens
                .stream()
                .filter(token -> token.getId().equals(activeToken.getId()))
                .findFirst()
                .orElseThrow();

        assertAll(
                () -> assertTrue(persistedReplacedToken.isRevoked()),
                () -> assertTrue(persistedReplacedToken.isReplaced()),
                () -> assertEquals(
                        replacementTokenId,
                        persistedReplacedToken.getReplacedByTokenId()
                ),
                () -> assertTrue(persistedActiveToken.isRevoked()),
                () -> assertNull(persistedActiveToken.getReplacedByTokenId())
        );
    }
}
