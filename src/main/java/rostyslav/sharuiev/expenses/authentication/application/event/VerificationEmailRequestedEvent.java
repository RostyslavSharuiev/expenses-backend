package rostyslav.sharuiev.expenses.authentication.application.event;

public record VerificationEmailRequestedEvent(
        String recipient,
        String rawToken
) {
}
