package rostyslav.sharuiev.expenses.authentication.application.port.in;

import java.util.UUID;

public interface AuthenticateUseCase {

    AuthenticationResult authenticate(AuthenticateCommand command);

    record AuthenticateCommand(
            String email,
            String password
    ) {}

    record AuthenticationResult(
            UUID userId,
            String accessToken,
            String refreshToken,
            long accessTokenExpiresIn
    ) {}
}
