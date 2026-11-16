package rostyslav.sharuiev.expenses.session.application.port.in;

import java.util.UUID;

public interface SessionManagementUseCase {

    SessionsTokens createSession(UUID userId);

    void revokeAllSessions(UUID userId);

    record SessionsTokens(
            String accessToken,
            String refreshToken,
            long accessTokenExpiresIn
    ) {}
}
