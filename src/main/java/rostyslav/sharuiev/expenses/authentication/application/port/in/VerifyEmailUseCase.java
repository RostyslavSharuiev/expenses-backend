package rostyslav.sharuiev.expenses.authentication.application.port.in;

public interface VerifyEmailUseCase {

    void verifyEmail(String rawToken);
}
