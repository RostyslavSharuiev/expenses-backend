package rostyslav.sharuiev.expenses.authentication.application.event;

public record PasswordResetEmailRequestedEvent(
        String recipient,
        String rawToken
) {
}
