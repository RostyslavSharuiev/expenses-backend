package rostyslav.sharuiev.expenses.authentication.application.port.in;

import java.util.UUID;

public interface AuthenticateUseCase {

    UUID authenticate(AuthenticateCommand command);

    record AuthenticateCommand(
            String email,
            String password
    ) {}
}
