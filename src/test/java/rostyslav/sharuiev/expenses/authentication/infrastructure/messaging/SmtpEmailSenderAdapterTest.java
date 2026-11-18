package rostyslav.sharuiev.expenses.authentication.infrastructure.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import rostyslav.sharuiev.expenses.authentication.infrastructure.config.EmailProperties;
import rostyslav.sharuiev.expenses.authentication.infrastructure.messaging.adapter.SmtpEmailSenderAdapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SmtpEmailSenderAdapterTest {

    private static final String FROM = "no-reply@expenses.local";
    private static final String FRONTEND_BASE_URL = "http://localhost:3000";

    private JavaMailSender mailSender;
    private SmtpEmailSenderAdapter adapter;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);

        EmailProperties emailProperties = new EmailProperties(FROM, FRONTEND_BASE_URL);

        adapter = new SmtpEmailSenderAdapter(mailSender, emailProperties);
    }

    @Test
    void shouldSendVerificationEmail() {
        String recipient = "user@example.com";
        String rawToken = "verification-token";

        adapter.sendVerificationEmail(recipient, rawToken);

        var messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();

        assertEquals(FROM, message.getFrom());
        assertArrayEquals(new String[]{recipient}, message.getTo());
        assertEquals("Verify your email address", message.getSubject());

        assert message.getText() != null;
        assertTrue(
                message
                        .getText()
                        .contains(FRONTEND_BASE_URL + "/verify-email?token=" + rawToken)
        );
    }

    @Test
    void shouldSendPasswordResetEmail() {
        String recipient = "user@example.com";
        String rawToken = "password-reset-token";

        adapter.sendPasswordResetEmail(recipient, rawToken);

        var messageCaptor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();

        assertEquals(FROM, message.getFrom());
        assertArrayEquals(new String[]{recipient}, message.getTo());
        assertEquals("Reset your password", message.getSubject());

        assert message.getText() != null;
        assertTrue(
                message
                        .getText()
                        .contains(FRONTEND_BASE_URL + "/reset-password?token=" + rawToken)
        );
    }

    @Test
    void shouldPropagateMailSendingException() {
        String recipient = "user@example.com";
        String rawToken = "verification-token";

        doThrow(new IllegalStateException("SMTP server unavailable"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        assertThrows(
                IllegalStateException.class,
                () -> adapter.sendVerificationEmail(recipient, rawToken)
        );
    }
}
