package rostyslav.sharuiev.expenses.authentication.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.authentication.application.port.in.VerifyEmailUseCase;
import rostyslav.sharuiev.expenses.authentication.application.port.out.EmailVerificationTokenRepositoryPort;
import rostyslav.sharuiev.expenses.authentication.application.port.out.TokenHasherPort;
import rostyslav.sharuiev.expenses.authentication.domain.model.EmailVerificationToken;
import rostyslav.sharuiev.expenses.user.application.port.in.UserAccountPort;

import java.time.Clock;
import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class VerifyEmailService implements VerifyEmailUseCase {

    private final EmailVerificationTokenRepositoryPort tokenRepositoryPort;
    private final TokenHasherPort tokenHasherPort;
    private final UserAccountPort userAccountPort;
    private final Clock clock;

    @Override
    public void verifyEmail(String rawToken) {
        String tokenHash = tokenHasherPort.hash(rawToken);

        EmailVerificationToken token = tokenRepositoryPort
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

        Instant now = clock.instant();

        if (!token.isUsableAt(now)) {
            throw new IllegalArgumentException("Invalid or expired verification token");
        }

        UserAccountPort.UserAccount user = userAccountPort
                .findById(token.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

        if (user.deleted() || user.emailVerifiedAt() != null) {
            throw new IllegalArgumentException("Invalid or expired verification token");
        }

        token.markAsUsed(now);
        tokenRepositoryPort.save(token);

        userAccountPort.verifyEmail(user.id(), now);
    }
}
