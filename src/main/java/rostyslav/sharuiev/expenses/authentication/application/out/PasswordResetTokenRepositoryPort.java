package rostyslav.sharuiev.expenses.authentication.application.out;

import rostyslav.sharuiev.expenses.authentication.domain.model.PasswordResetToken;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepositoryPort {

    PasswordResetToken save(PasswordResetToken token);

    Optional<PasswordResetToken> findByTokenHashForUpdate(String tokenHash);

    void invalidateAllByUserId(UUID userId);
}
