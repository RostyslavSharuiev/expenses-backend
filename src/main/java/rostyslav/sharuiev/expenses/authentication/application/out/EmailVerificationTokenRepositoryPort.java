package rostyslav.sharuiev.expenses.authentication.application.out;

import rostyslav.sharuiev.expenses.authentication.domain.model.EmailVerificationToken;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepositoryPort {

    EmailVerificationToken save(EmailVerificationToken token);

    Optional<EmailVerificationToken> findByTokenHashForUpdate(String tokenHash);

    void invalidateAllByUserId(UUID userId);
}
