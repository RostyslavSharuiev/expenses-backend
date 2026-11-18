package rostyslav.sharuiev.expenses.authentication.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import rostyslav.sharuiev.expenses.authentication.application.event.PasswordResetEmailRequestedEvent;
import rostyslav.sharuiev.expenses.authentication.application.port.out.PasswordResetTokenRepositoryPort;
import rostyslav.sharuiev.expenses.authentication.domain.model.PasswordResetToken;
import rostyslav.sharuiev.expenses.authentication.infrastructure.config.AuthenticationProperties;
import rostyslav.sharuiev.expenses.shared.application.port.out.SecureTokenGeneratorPort;
import rostyslav.sharuiev.expenses.shared.application.port.out.TokenHasherPort;
import rostyslav.sharuiev.expenses.user.application.port.in.UserAccountPort;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-18T09:00:00Z");
    private static final UUID USER_ID = UUID.randomUUID();

    private static final String EMAIL = "user@example.com";
    private static final String RAW_TOKEN = "new-password-reset-token";
    private static final String TOKEN_HASH = "hashed-password-reset-token";

    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

    @Mock
    private UserAccountPort userAccountPort;

    @Mock
    private PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;

    @Mock
    private SecureTokenGeneratorPort secureTokenGeneratorPort;

    @Mock
    private TokenHasherPort tokenHasherPort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ForgotPasswordService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

        AuthenticationProperties properties = new AuthenticationProperties(
                Duration.ofHours(24),
                TOKEN_TTL,
                "VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdTZWNyZXRLZXlGb3JUZXN0aW5n",
                Duration.ofMinutes(15),
                "expenses-api"
        );

        service = new ForgotPasswordService(
                userAccountPort,
                passwordResetTokenRepositoryPort,
                secureTokenGeneratorPort,
                tokenHasherPort,
                eventPublisher,
                properties,
                clock
        );
    }

    @Test
    void shouldSendPasswordResetEmailSuccessfully() {
        UserAccountPort.UserAccount user = createUser();

        when(userAccountPort.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(secureTokenGeneratorPort.generateToken()).thenReturn(RAW_TOKEN);
        when(tokenHasherPort.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);

        service.forgotPassword(EMAIL);

        verify(passwordResetTokenRepositoryPort).invalidateAllByUserId(USER_ID);

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);

        verify(passwordResetTokenRepositoryPort).save(tokenCaptor.capture());

        PasswordResetToken savedToken = tokenCaptor.getValue();

        assertEquals(USER_ID, savedToken.getUserId());
        assertEquals(TOKEN_HASH, savedToken.getTokenHash());
        assertEquals(NOW.plus(TOKEN_TTL), savedToken.getExpiresAt());

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

        verify(eventPublisher).publishEvent(eventCaptor.capture());

        PasswordResetEmailRequestedEvent event = (PasswordResetEmailRequestedEvent) eventCaptor.getValue();

        assertEquals(EMAIL, event.recipient());
        assertEquals(RAW_TOKEN, event.rawToken());
    }

    @Test
    void shouldNormalizeEmailBeforeSearching() {
        UserAccountPort.UserAccount user = createUser();

        when(userAccountPort.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(secureTokenGeneratorPort.generateToken()).thenReturn(RAW_TOKEN);
        when(tokenHasherPort.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);

        service.forgotPassword("  USER@EXAMPLE.COM  ");

        verify(userAccountPort).findByEmail(EMAIL);
    }

    @Test
    void shouldDoNothingWhenUserDoesNotExist() {
        when(userAccountPort.findByEmail(EMAIL)).thenReturn(Optional.empty());

        service.forgotPassword(EMAIL);

        verifyNoInteractions(
                passwordResetTokenRepositoryPort,
                secureTokenGeneratorPort,
                tokenHasherPort,
                eventPublisher
        );
    }

    @Test
    void shouldDoNothingWhenUserIsDeleted() {
        UserAccountPort.UserAccount user = new UserAccountPort.UserAccount(
                USER_ID,
                EMAIL,
                null,
                true
        );

        when(userAccountPort.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        service.forgotPassword(EMAIL);

        verifyNoInteractions(
                passwordResetTokenRepositoryPort,
                secureTokenGeneratorPort,
                tokenHasherPort,
                eventPublisher
        );
    }

    @Test
    void shouldRejectNullEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.forgotPassword(null)
        );

        verifyNoInteractions(userAccountPort);
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.forgotPassword("   ")
        );

        verifyNoInteractions(userAccountPort);
    }

    private UserAccountPort.UserAccount createUser() {
        return new UserAccountPort.UserAccount(
                USER_ID,
                EMAIL,
                null,
                false
        );
    }
}
