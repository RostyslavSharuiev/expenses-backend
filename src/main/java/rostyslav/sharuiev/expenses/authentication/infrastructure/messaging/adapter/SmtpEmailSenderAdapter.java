package rostyslav.sharuiev.expenses.authentication.infrastructure.messaging.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import rostyslav.sharuiev.expenses.authentication.application.port.out.EmailSenderPort;
import rostyslav.sharuiev.expenses.authentication.infrastructure.config.EmailProperties;

@Component
@RequiredArgsConstructor
public class SmtpEmailSenderAdapter implements EmailSenderPort {

    private static final String VERIFICATION_EMAIL_SUBJECT = "Verify your email address";
    private static final String PASSWORD_RESET_EMAIL_SUBJECT = "Reset your password";

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;

    @Override
    public void sendVerificationEmail(String recipient, String rawToken) {
        String verificationUrl = String.format(
                "%s/verify-email?token=%s",
                emailProperties.frontendBaseUrl(),
                rawToken
        );

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(emailProperties.from());
        message.setTo(recipient);
        message.setSubject(VERIFICATION_EMAIL_SUBJECT);
        message.setText(String.format(
                """
                Hello!
                
                Please verify your email address by following this link:
                
                %s
                
                If you did not create an account, you can ignore this email.
                """,
                verificationUrl
        ));

        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetEmail(String recipient, String rawToken) {
        String passwordResetUrl = String.format(
                "%s/reset-password?token=%s",
                emailProperties.frontendBaseUrl(),
                rawToken
        );

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(emailProperties.from());
        message.setTo(recipient);
        message.setSubject(PASSWORD_RESET_EMAIL_SUBJECT);
        message.setText(String.format(
                """
                Hello!

                You requested a password reset.

                Follow this link to reset your password:

                %s

                If you did not request a password reset, you can ignore this email.
                """,
                passwordResetUrl
        ));

        mailSender.send(message);
    }
}
