package rostyslav.sharuiev.expenses.authentication.application.in;

public interface ResetPasswordUseCase {

    ResetPasswordResult resetPassword(ResetPasswordCommand command);

    record ResetPasswordCommand(
            String rawToken,
            String newPassword
    ) {}

    record ResetPasswordResult(
            String accessToken,
            String refreshToken,
            long accessTokenExpiresIn
    ) {}
}
