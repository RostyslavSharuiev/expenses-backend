package rostyslav.sharuiev.expenses.authentication.application.port.out;

public interface EmailSenderPort {

    void sendVerificationEmail(
            String recipient,
            String rawToken
    );

    void sendPasswordResetEmail(
            String recipient,
            String rawToken
    );
}
