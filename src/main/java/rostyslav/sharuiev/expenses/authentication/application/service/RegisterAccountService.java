package rostyslav.sharuiev.expenses.authentication.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.authentication.application.event.VerificationEmailRequestedEvent;
import rostyslav.sharuiev.expenses.authentication.application.port.in.RegisterAccountUseCase;
import rostyslav.sharuiev.expenses.authentication.application.port.out.*;
import rostyslav.sharuiev.expenses.authentication.domain.model.EmailVerificationToken;
import rostyslav.sharuiev.expenses.authentication.domain.model.UserCredentials;
import rostyslav.sharuiev.expenses.authentication.infrastructure.config.AuthenticationProperties;
import rostyslav.sharuiev.expenses.shared.application.port.out.SecureTokenGeneratorPort;
import rostyslav.sharuiev.expenses.user.application.port.in.UserAccountPort;
import rostyslav.sharuiev.expenses.shared.application.port.out.TokenHasherPort;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RegisterAccountService implements RegisterAccountUseCase {

    private final UserAccountPort userAccountPort;

    private final UserCredentialsRepositoryPort userCredentialsRepositoryPort;
    private final EmailVerificationTokenRepositoryPort emailVerificationTokenRepositoryPort;

    private final PasswordEncoderPort passwordEncoderPort;
    private final SecureTokenGeneratorPort secureTokenGeneratorPort;
    private final TokenHasherPort tokenHasherPort;

    private final ApplicationEventPublisher eventPublisher;

    private final Clock clock;
    private final AuthenticationProperties properties;


    @Override
    @Transactional
    public RegistrationResult register(RegisterAccountCommand command) {
        Instant now = clock.instant();

        // 1. Create user account
        var user = userAccountPort.create(
                new UserAccountPort.CreateUserCommand(
                        command.email(),
                        command.defaultCurrency()
                )
        );

        // 2. Encode password
        String passwordHash = passwordEncoderPort.encode(command.password());

        // 3. Save credentials
        UserCredentials credentials = UserCredentials.create(
                user.id(),
                passwordHash,
                now
        );

        userCredentialsRepositoryPort.save(credentials);

        // 4. Generate verification token
        String rawToken = secureTokenGeneratorPort.generateToken();
        String tokenHash = tokenHasherPort.hash(rawToken);

        EmailVerificationToken verificationToken = EmailVerificationToken.create(
                user.id(),
                tokenHash,
                now.plus(properties.emailVerificationTokenTtl()),
                now
        );

        emailVerificationTokenRepositoryPort.save(verificationToken);

        // 5. Publish event for email delivery after transaction commit
        eventPublisher.publishEvent(
                new VerificationEmailRequestedEvent(
                        user.email(),
                        rawToken
                )
        );

        return new RegistrationResult(
                user.id(),
                user.email()
        );
    }
}
