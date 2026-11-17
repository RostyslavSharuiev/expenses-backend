package rostyslav.sharuiev.expenses.session.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.session.application.exception.RefreshTokenReuseException;
import rostyslav.sharuiev.expenses.session.application.port.in.RotateRefreshTokenUseCase;
import rostyslav.sharuiev.expenses.session.application.port.out.RefreshTokenRepositoryPort;
import rostyslav.sharuiev.expenses.shared.application.port.out.SecureTokenGeneratorPort;
import rostyslav.sharuiev.expenses.session.domain.model.RefreshToken;
import rostyslav.sharuiev.expenses.session.infrastructure.config.SessionProperties;
import rostyslav.sharuiev.expenses.shared.application.port.out.TokenHasherPort;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenRotationService implements RotateRefreshTokenUseCase {

    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final SecureTokenGeneratorPort secureTokenGeneratorPort;
    private final TokenHasherPort tokenHasherPort;
    private final SessionProperties sessionProperties;
    private final Clock clock;

    @Override
    @Transactional(noRollbackFor = RefreshTokenReuseException.class)
    public String rotateRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token must not be blank");
        }

        Instant now = clock.instant();

        String tokenHash = tokenHasherPort.hash(rawRefreshToken);

        RefreshToken currentToken = refreshTokenRepositoryPort
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired refresh token"));

        if (currentToken.isReplaced()) {
            refreshTokenRepositoryPort.revokeAllBySessionId(
                    currentToken.getSessionId()
            );

            throw new RefreshTokenReuseException();
        }

        if (!currentToken.isUsableAt(now)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String newRawToken = secureTokenGeneratorPort.generateToken();
        String newTokenHash = tokenHasherPort.hash(newRawToken);

        RefreshToken newToken = RefreshToken.create(
                currentToken.getUserId(),
                currentToken.getSessionId(),
                newTokenHash,
                now.plus(sessionProperties.refreshTokenTtl()),
                now
        );

        currentToken.replaceWith(newToken.getId(), now);

        refreshTokenRepositoryPort.save(currentToken);
        refreshTokenRepositoryPort.save(newToken);

        return newRawToken;
    }
}
