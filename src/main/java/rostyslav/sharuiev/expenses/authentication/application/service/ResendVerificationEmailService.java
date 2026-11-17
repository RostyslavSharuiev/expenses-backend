package rostyslav.sharuiev.expenses.authentication.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.authentication.application.event.VerificationEmailRequestedEvent;
import rostyslav.sharuiev.expenses.authentication.application.port.in.ResendVerificationEmailUseCase;
import rostyslav.sharuiev.expenses.authentication.application.port.out.EmailVerificationTokenRepositoryPort;
import rostyslav.sharuiev.expenses.authentication.domain.model.EmailVerificationToken;
import rostyslav.sharuiev.expenses.authentication.infrastructure.config.AuthenticationProperties;
import rostyslav.sharuiev.expenses.shared.application.port.out.SecureTokenGeneratorPort;
import rostyslav.sharuiev.expenses.shared.application.port.out.TokenHasherPort;
import rostyslav.sharuiev.expenses.user.application.port.in.UserAccountPort;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ResendVerificationEmailService implements ResendVerificationEmailUseCase {

    private final UserAccountPort userAccountPort;
    private final EmailVerificationTokenRepositoryPort tokenRepositoryPort;
    private final SecureTokenGeneratorPort secureTokenGeneratorPort;
    private final TokenHasherPort tokenHasherPort;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthenticationProperties authenticationProperties;
    private final Clock clock;

    @Override
    public void resendVerificationEmail(String email) {
        Optional<UserAccountPort.UserAccount> userOptional = userAccountPort.findByEmail(email);

        if (userOptional.isEmpty()) {
            return;
        }

        UserAccountPort.UserAccount user = userOptional.get();

        if (user.deleted() || user.emailVerifiedAt() != null) {
            return;
        }

        Instant now = clock.instant();

        tokenRepositoryPort.invalidateAllByUserId(user.id());

        String rawToken = secureTokenGeneratorPort.generateToken();
        String tokenHash = tokenHasherPort.hash(rawToken);

        EmailVerificationToken token = EmailVerificationToken.create(
                user.id(),
                tokenHash,
                now.plus(authenticationProperties.emailVerificationTokenTtl()),
                now
        );

        tokenRepositoryPort.save(token);

        eventPublisher.publishEvent(
                new VerificationEmailRequestedEvent(user.email(), rawToken)
        );
    }
}
