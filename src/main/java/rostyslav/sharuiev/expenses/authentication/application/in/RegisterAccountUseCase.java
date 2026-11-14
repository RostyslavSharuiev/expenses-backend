package rostyslav.sharuiev.expenses.authentication.application.in;


import java.util.UUID;

public interface RegisterAccountUseCase {

    RegistrationResult register(RegisterAccountCommand command);

    record RegisterAccountCommand(
            String email,
            String password,
            String defaultCurrency
    ) {}


    record RegistrationResult(
            UUID userId,
            String email
    ) {}
}
