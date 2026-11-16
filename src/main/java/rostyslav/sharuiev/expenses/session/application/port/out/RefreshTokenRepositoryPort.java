package rostyslav.sharuiev.expenses.session.application.port.out;

import rostyslav.sharuiev.expenses.session.domain.model.RefreshToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositoryPort {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findById(UUID id);

    Optional<RefreshToken> findByTokenHashForUpdate(String tokenHash);

    List<RefreshToken> findAllBySessionId(UUID sessionId);

    void revokeAllBySessionId(UUID sessionId);
}
