package rostyslav.sharuiev.expenses.session.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.session.application.port.in.SessionManagementUseCase;
import rostyslav.sharuiev.expenses.session.application.port.out.RefreshTokenRepositoryPort;
import rostyslav.sharuiev.expenses.session.application.port.out.SecureTokenGeneratorPort;
import rostyslav.sharuiev.expenses.session.application.port.out.TokenHasherPort;
import rostyslav.sharuiev.expenses.session.domain.model.RefreshToken;
import rostyslav.sharuiev.expenses.session.infrastructure.config.SessionProperties;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class SessionManagementService implements SessionManagementUseCase {

    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final SecureTokenGeneratorPort secureTokenGeneratorPort;
    private final TokenHasherPort tokenHasherPort;
    private final SessionProperties sessionProperties;
    private final Clock clock;

    @Override
    public String createSession(UUID userId) {
        Instant now = clock.instant();

        String rawToken = secureTokenGeneratorPort.generateToken();
        String tokenHash = tokenHasherPort.hash(rawToken);

        RefreshToken refreshToken = RefreshToken.create(
                userId,
                UUID.randomUUID(),
                tokenHash,
                now.plus(sessionProperties.refreshTokenTtl()),
                now
        );

        refreshTokenRepositoryPort.save(refreshToken);

        return rawToken;
    }

    @Override
    public void revokeAllSessions(UUID userId) {
        refreshTokenRepositoryPort.revokeAllByUserId(userId);
    }
}
