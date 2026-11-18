package rostyslav.sharuiev.expenses.session.application.port.in;

import java.util.UUID;

public interface SessionManagementUseCase {

    String createSession(UUID userId);

    void revokeSession(String rawRefreshToken);

    void revokeAllSessions(UUID userId);
}
