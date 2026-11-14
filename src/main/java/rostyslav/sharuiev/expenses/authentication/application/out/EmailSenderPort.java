package rostyslav.sharuiev.expenses.authentication.application.out;

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
