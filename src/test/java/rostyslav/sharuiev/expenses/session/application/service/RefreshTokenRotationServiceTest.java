package rostyslav.sharuiev.expenses.session.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rostyslav.sharuiev.expenses.session.application.exception.InvalidRefreshTokenException;
import rostyslav.sharuiev.expenses.session.application.exception.RefreshTokenReuseException;
import rostyslav.sharuiev.expenses.session.application.port.in.RotateRefreshTokenUseCase;
import rostyslav.sharuiev.expenses.session.application.port.out.RefreshTokenRepositoryPort;
import rostyslav.sharuiev.expenses.session.domain.model.RefreshToken;
import rostyslav.sharuiev.expenses.session.infrastructure.config.SessionProperties;
import rostyslav.sharuiev.expenses.shared.application.port.out.SecureTokenGeneratorPort;
import rostyslav.sharuiev.expenses.shared.application.port.out.TokenHasherPort;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRotationServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-19T10:00:00Z");
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

    private final UUID userId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();

    private final String rawRefreshToken = "old-raw-token";
    private final String oldTokenHash = "old-token-hash";

    private final String newRawRefreshToken = "new-raw-token";
    private final String newTokenHash = "new-token-hash";

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    @Mock
    private SecureTokenGeneratorPort secureTokenGeneratorPort;

    @Mock
    private TokenHasherPort tokenHasherPort;

    private RefreshTokenRotationService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenRotationService(
                refreshTokenRepositoryPort,
                secureTokenGeneratorPort,
                tokenHasherPort,
                new SessionProperties(REFRESH_TOKEN_TTL),
                clock
        );
    }

    @Test
    void rotateRefreshToken_shouldRotateTokenSuccessfully() {
        RefreshToken currentToken = RefreshToken.create(
                userId,
                sessionId,
                oldTokenHash,
                NOW.plus(Duration.ofDays(10)),
                NOW.minus(Duration.ofDays(10))
        );

        when(tokenHasherPort.hash(rawRefreshToken)).thenReturn(oldTokenHash);
        when(refreshTokenRepositoryPort.findByTokenHashForUpdate(oldTokenHash)).thenReturn(Optional.of(currentToken));
        when(secureTokenGeneratorPort.generateToken()).thenReturn(newRawRefreshToken);
        when(tokenHasherPort.hash(newRawRefreshToken)).thenReturn(newTokenHash);

        RotateRefreshTokenUseCase.RotationResult result = service.rotateRefreshToken(rawRefreshToken);

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepositoryPort, times(2)).save(tokenCaptor.capture());

        RefreshToken savedOldToken = tokenCaptor.getAllValues().get(0);
        RefreshToken savedNewToken = tokenCaptor.getAllValues().get(1);

        assertAll(
                () -> assertEquals(userId, result.userId()),
                () -> assertEquals(newRawRefreshToken, result.refreshToken()),

                () -> assertEquals(currentToken.getId(), savedOldToken.getId()),
                () -> assertTrue(savedOldToken.isRevoked()),
                () -> assertTrue(savedOldToken.isReplaced()),
                () -> assertEquals(NOW, savedOldToken.getRevokedAt()),
                () -> assertEquals(savedNewToken.getId(), savedOldToken.getReplacedByTokenId()),

                () -> assertEquals(userId, savedNewToken.getUserId()),
                () -> assertEquals(sessionId, savedNewToken.getSessionId()),
                () -> assertEquals(newTokenHash, savedNewToken.getTokenHash()),
                () -> assertEquals(NOW, savedNewToken.getCreatedAt()),
                () -> assertEquals(NOW.plus(REFRESH_TOKEN_TTL), savedNewToken.getExpiresAt()),
                () -> assertFalse(savedNewToken.isRevoked())
        );
    }

    @Test
    void rotateRefreshToken_shouldRejectNullToken() {
        assertThrows(
                InvalidRefreshTokenException.class,
                () -> service.rotateRefreshToken(null)
        );

        verifyNoInteractions(
                refreshTokenRepositoryPort,
                secureTokenGeneratorPort,
                tokenHasherPort
        );
    }

    @Test
    void rotateRefreshToken_shouldRejectBlankToken() {
        assertThrows(
                InvalidRefreshTokenException.class,
                () -> service.rotateRefreshToken("")
        );

        verifyNoInteractions(
                refreshTokenRepositoryPort,
                secureTokenGeneratorPort,
                tokenHasherPort
        );
    }

    @Test
    void rotateRefreshToken_shouldRejectWhitespaceToken() {
        assertThrows(
                InvalidRefreshTokenException.class,
                () -> service.rotateRefreshToken("   ")
        );

        verifyNoInteractions(
                refreshTokenRepositoryPort,
                secureTokenGeneratorPort,
                tokenHasherPort
        );
    }

    @Test
    void rotateRefreshToken_shouldRejectUnknownToken() {
        when(tokenHasherPort.hash(rawRefreshToken)).thenReturn(oldTokenHash);
        when(refreshTokenRepositoryPort.findByTokenHashForUpdate(oldTokenHash)).thenReturn(Optional.empty());

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> service.rotateRefreshToken(rawRefreshToken)
        );

        verify(tokenHasherPort).hash(rawRefreshToken);
        verify(refreshTokenRepositoryPort).findByTokenHashForUpdate(oldTokenHash);

        verifyNoMoreInteractions(
                refreshTokenRepositoryPort,
                secureTokenGeneratorPort
        );
    }

    @Test
    void rotateRefreshToken_shouldRejectExpiredToken() {
        RefreshToken expiredToken = RefreshToken.create(
                userId,
                sessionId,
                oldTokenHash,
                NOW.minusSeconds(1),
                NOW.minus(Duration.ofDays(10))
        );

        when(tokenHasherPort.hash(rawRefreshToken)).thenReturn(oldTokenHash);
        when(refreshTokenRepositoryPort.findByTokenHashForUpdate(oldTokenHash)).thenReturn(Optional.of(expiredToken));

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> service.rotateRefreshToken(rawRefreshToken)
        );

        verify(tokenHasherPort).hash(rawRefreshToken);
        verify(refreshTokenRepositoryPort).findByTokenHashForUpdate(oldTokenHash);

        verifyNoMoreInteractions(
                refreshTokenRepositoryPort,
                secureTokenGeneratorPort
        );
    }

    @Test
    void rotateRefreshToken_shouldRejectRevokedToken() {
        RefreshToken revokedToken = RefreshToken.reconstitute(
                UUID.randomUUID(),
                userId,
                sessionId,
                oldTokenHash,
                NOW.plus(Duration.ofDays(10)),
                NOW.minus(Duration.ofDays(10)),
                NOW.minusSeconds(1),
                null
        );

        when(tokenHasherPort.hash(rawRefreshToken)).thenReturn(oldTokenHash);
        when(refreshTokenRepositoryPort.findByTokenHashForUpdate(oldTokenHash)).thenReturn(Optional.of(revokedToken));

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> service.rotateRefreshToken(rawRefreshToken)
        );

        verify(tokenHasherPort).hash(rawRefreshToken);
        verify(refreshTokenRepositoryPort).findByTokenHashForUpdate(oldTokenHash);

        verifyNoMoreInteractions(
                refreshTokenRepositoryPort,
                secureTokenGeneratorPort
        );
    }

    @Test
    void rotateRefreshToken_shouldRevokeSessionWhenReplacedTokenIsReused() {
        RefreshToken replacedToken = RefreshToken.reconstitute(
                UUID.randomUUID(),
                userId,
                sessionId,
                oldTokenHash,
                NOW.plus(Duration.ofDays(10)),
                NOW.minus(Duration.ofDays(10)),
                NOW.minusSeconds(1),
                UUID.randomUUID()
        );

        when(tokenHasherPort.hash(rawRefreshToken)).thenReturn(oldTokenHash);
        when(refreshTokenRepositoryPort.findByTokenHashForUpdate(oldTokenHash)).thenReturn(Optional.of(replacedToken));

        assertThrows(
                RefreshTokenReuseException.class,
                () -> service.rotateRefreshToken(rawRefreshToken)
        );

        verify(tokenHasherPort).hash(rawRefreshToken);
        verify(refreshTokenRepositoryPort).findByTokenHashForUpdate(oldTokenHash);
        verify(refreshTokenRepositoryPort).revokeAllBySessionId(sessionId);
        verify(refreshTokenRepositoryPort, never()).save(any());

        verifyNoInteractions(secureTokenGeneratorPort);
    }
}
