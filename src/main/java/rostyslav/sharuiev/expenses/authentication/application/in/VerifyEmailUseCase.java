package rostyslav.sharuiev.expenses.authentication.application.in;

public interface VerifyEmailUseCase {

    void verifyEmail(String rawToken);
}
