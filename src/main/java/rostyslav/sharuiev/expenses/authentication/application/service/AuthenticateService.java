package rostyslav.sharuiev.expenses.authentication.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.authentication.application.exception.InvalidCredentialsException;
import rostyslav.sharuiev.expenses.authentication.application.port.in.AuthenticateUseCase;
import rostyslav.sharuiev.expenses.authentication.application.port.out.AccessTokenProviderPort;
import rostyslav.sharuiev.expenses.authentication.application.port.out.PasswordEncoderPort;
import rostyslav.sharuiev.expenses.authentication.application.port.out.UserCredentialsRepositoryPort;
import rostyslav.sharuiev.expenses.session.application.port.in.SessionManagementUseCase;
import rostyslav.sharuiev.expenses.user.application.port.in.UserAccountPort;

import java.util.Locale;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthenticateService implements AuthenticateUseCase {

    private final UserAccountPort userAccountPort;
    private final UserCredentialsRepositoryPort userCredentialsRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final SessionManagementUseCase sessionManagementUseCase;
    private final AccessTokenProviderPort accessTokenProviderPort;


    @Override
    public AuthenticationResult authenticate(AuthenticateCommand command) {
        validateCommand(command);

        String email = command.email().trim().toLowerCase(Locale.ROOT);

        UserAccountPort.UserAccount user = userAccountPort
                .findByEmail(email)
                .filter(account -> !account.deleted())
                .filter(account -> account.emailVerifiedAt() != null)
                .orElseThrow(InvalidCredentialsException::new);

        var credentials = userCredentialsRepositoryPort
                .findByUserId(user.id())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoderPort.matches(command.password(), credentials.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String refreshToken = sessionManagementUseCase.createSession(user.id());

        AccessTokenProviderPort.AccessToken accessToken = accessTokenProviderPort.generate(user.id());

        return new AuthenticationResult(
                user.id(),
                accessToken.value(),
                refreshToken,
                accessToken.expiresIn()
        );
    }

    private void validateCommand(AuthenticateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command must not be null");
        }

        if (command.email() == null || command.email().isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }

        if (command.password() == null || command.password().isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
    }
}
