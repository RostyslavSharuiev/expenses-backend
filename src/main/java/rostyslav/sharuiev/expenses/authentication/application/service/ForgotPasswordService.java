package rostyslav.sharuiev.expenses.authentication.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.authentication.application.event.PasswordResetEmailRequestedEvent;
import rostyslav.sharuiev.expenses.authentication.application.port.in.ForgotPasswordUseCase;
import rostyslav.sharuiev.expenses.authentication.application.port.out.PasswordResetTokenRepositoryPort;
import rostyslav.sharuiev.expenses.authentication.domain.model.PasswordResetToken;
import rostyslav.sharuiev.expenses.authentication.infrastructure.config.AuthenticationProperties;
import rostyslav.sharuiev.expenses.shared.application.port.out.SecureTokenGeneratorPort;
import rostyslav.sharuiev.expenses.shared.application.port.out.TokenHasherPort;
import rostyslav.sharuiev.expenses.user.application.port.in.UserAccountPort;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ForgotPasswordService implements ForgotPasswordUseCase {

    private final UserAccountPort userAccountPort;
    private final PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;
    private final SecureTokenGeneratorPort secureTokenGeneratorPort;
    private final TokenHasherPort tokenHasherPort;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthenticationProperties authenticationProperties;
    private final Clock clock;

    @Override
    public void forgotPassword(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        Optional<UserAccountPort.UserAccount> userOptional = userAccountPort.findByEmail(normalizedEmail);

        if (userOptional.isEmpty()) {
            return;
        }

        UserAccountPort.UserAccount user = userOptional.get();

        if (user.deleted()) {
            return;
        }

        Instant now = clock.instant();

        passwordResetTokenRepositoryPort.invalidateAllByUserId(user.id());

        String rawToken = secureTokenGeneratorPort.generateToken();
        String tokenHash = tokenHasherPort.hash(rawToken);

        PasswordResetToken token = PasswordResetToken.create(
                user.id(),
                tokenHash,
                now.plus(authenticationProperties.passwordResetTokenTtl()),
                now
        );

        passwordResetTokenRepositoryPort.save(token);

        eventPublisher.publishEvent(
                new PasswordResetEmailRequestedEvent(user.email(), rawToken)
        );
    }
}
