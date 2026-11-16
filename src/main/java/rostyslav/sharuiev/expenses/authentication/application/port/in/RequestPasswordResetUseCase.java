package rostyslav.sharuiev.expenses.authentication.application.port.in;

public interface RequestPasswordResetUseCase {

    void requestPasswordReset(String email);
}
