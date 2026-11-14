package rostyslav.sharuiev.expenses.authentication.application.out;

import java.util.UUID;

public interface SessionManagementPort {

    SessionsTokens createSession(UUID userId);

    void revokeAllSessions(UUID userId);

    record SessionsTokens(
            String accessToken,
            String refreshToken,
            long accessTokenExpiresIn
    ) {}
}
