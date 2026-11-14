package rostyslav.sharuiev.expenses.authentication.application.in;

public interface ResetPasswordUseCase {

    void resetPassword(ResetPasswordCommand command);

    record ResetPasswordCommand(
            String rawToken,
            String newPassword
    ) {}
}
