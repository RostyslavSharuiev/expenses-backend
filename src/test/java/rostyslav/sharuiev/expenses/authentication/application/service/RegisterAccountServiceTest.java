package rostyslav.sharuiev.expenses.authentication.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import rostyslav.sharuiev.expenses.authentication.application.event.VerificationEmailRequestedEvent;
import rostyslav.sharuiev.expenses.authentication.application.port.in.RegisterAccountUseCase.RegisterAccountCommand;
import rostyslav.sharuiev.expenses.authentication.application.port.in.RegisterAccountUseCase.RegistrationResult;
import rostyslav.sharuiev.expenses.authentication.application.port.out.EmailVerificationTokenRepositoryPort;
import rostyslav.sharuiev.expenses.authentication.application.port.out.PasswordEncoderPort;
import rostyslav.sharuiev.expenses.authentication.application.port.out.UserCredentialsRepositoryPort;
import rostyslav.sharuiev.expenses.authentication.domain.model.EmailVerificationToken;
import rostyslav.sharuiev.expenses.authentication.domain.model.UserCredentials;
import rostyslav.sharuiev.expenses.authentication.infrastructure.config.AuthenticationProperties;
import rostyslav.sharuiev.expenses.shared.application.port.out.SecureTokenGeneratorPort;
import rostyslav.sharuiev.expenses.shared.application.port.out.TokenHasherPort;
import rostyslav.sharuiev.expenses.user.application.port.in.UserAccountPort;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterAccountServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-17T10:00:00Z");
    private static final Duration TOKEN_TTL = Duration.ofHours(24);
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String JWT_SECRET = "dGVzdC1qd3Qtc2VjcmV0LXRoYXQtaXMtYXQtMzItYnl0ZXM=";

    @Mock
    private UserAccountPort userAccountPort;

    @Mock
    private UserCredentialsRepositoryPort userCredentialsRepositoryPort;

    @Mock
    private EmailVerificationTokenRepositoryPort emailVerificationTokenRepositoryPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private SecureTokenGeneratorPort secureTokenGeneratorPort;

    @Mock
    private TokenHasherPort tokenHasherPort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private RegisterAccountService registerAccountService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

        AuthenticationProperties properties = new AuthenticationProperties(
                TOKEN_TTL,
                Duration.ofMinutes(30),
                JWT_SECRET,
                Duration.ofMinutes(15),
                "expenses-api"
        );

        registerAccountService = new RegisterAccountService(
                userAccountPort,
                userCredentialsRepositoryPort,
                emailVerificationTokenRepositoryPort,
                passwordEncoderPort,
                secureTokenGeneratorPort,
                tokenHasherPort,
                eventPublisher,
                clock,
                properties
        );
    }

    @Test
    void shouldRegisterAccountSuccessfully() {
        String email = "user@example.com";
        String rawPassword = "Password123!";
        String passwordHash = "encoded-password";
        String rawToken = "raw-verification-token";
        String tokenHash = "hashed-verification-token";

        RegisterAccountCommand command = new RegisterAccountCommand(
                email,
                rawPassword,
                "USD"
        );

        UserAccountPort.UserAccount user = new UserAccountPort.UserAccount(
                USER_ID,
                email,
                null,
                false
        );

        when(userAccountPort.create(any(UserAccountPort.CreateUserCommand.class))).thenReturn(user);
        when(passwordEncoderPort.encode(rawPassword)).thenReturn(passwordHash);
        when(secureTokenGeneratorPort.generateToken()).thenReturn(rawToken);
        when(tokenHasherPort.hash(rawToken)).thenReturn(tokenHash);

        RegistrationResult result = registerAccountService.register(command);

        assertEquals(USER_ID, result.userId());
        assertEquals(email, result.email());

        ArgumentCaptor<UserAccountPort.CreateUserCommand> userCommandCaptor = ArgumentCaptor
                .forClass(UserAccountPort.CreateUserCommand.class);

        verify(userAccountPort).create(userCommandCaptor.capture());

        assertEquals(email, userCommandCaptor.getValue().email());
        assertEquals("USD", userCommandCaptor.getValue().defaultCurrency());

        ArgumentCaptor<UserCredentials> credentialsCaptor = ArgumentCaptor
                .forClass(UserCredentials.class);

        verify(userCredentialsRepositoryPort).save(credentialsCaptor.capture());

        UserCredentials credentials = credentialsCaptor.getValue();

        assertEquals(USER_ID, credentials.getUserId());
        assertEquals(passwordHash, credentials.getPasswordHash());
        assertEquals(NOW, credentials.getPasswordChangedAt());

        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor
                .forClass(EmailVerificationToken.class);

        verify(emailVerificationTokenRepositoryPort).save(tokenCaptor.capture());

        EmailVerificationToken token = tokenCaptor.getValue();

        assertEquals(USER_ID, token.getUserId());
        assertEquals(tokenHash, token.getTokenHash());
        assertEquals(NOW, token.getCreatedAt());
        assertEquals(NOW.plus(TOKEN_TTL), token.getExpiresAt());

        ArgumentCaptor<VerificationEmailRequestedEvent> eventCaptor = ArgumentCaptor
                .forClass(VerificationEmailRequestedEvent.class);

        verify(eventPublisher).publishEvent(eventCaptor.capture());

        assertEquals(email, eventCaptor.getValue().recipient());
        assertEquals(rawToken, eventCaptor.getValue().rawToken());

        verify(passwordEncoderPort).encode(rawPassword);
        verify(secureTokenGeneratorPort).generateToken();
        verify(tokenHasherPort).hash(rawToken);
    }

    @Test
    void shouldRejectNullCommand() {
        assertThrows(
                IllegalArgumentException.class,
                () -> registerAccountService.register(null)
        );

        verifyNoInteractions(
                userAccountPort,
                userCredentialsRepositoryPort,
                emailVerificationTokenRepositoryPort,
                passwordEncoderPort,
                secureTokenGeneratorPort,
                tokenHasherPort,
                eventPublisher
        );
    }

    @Test
    void shouldRejectBlankEmail() {
        assertInvalidCommand(new RegisterAccountCommand(
                " ",
                "Password123!",
                "USD"
        ));
    }

    @Test
    void shouldRejectNullEmail() {
        assertInvalidCommand(new RegisterAccountCommand(
                null,
                "Password123!",
                "USD"
        ));
    }

    @Test
    void shouldRejectBlankPassword() {
        assertInvalidCommand(new RegisterAccountCommand(
                "user@example.com",
                " ",
                "USD"
        ));
    }

    @Test
    void shouldRejectNullPassword() {
        assertInvalidCommand(new RegisterAccountCommand(
                "user@example.com",
                null,
                "USD"
        ));
    }

    @Test
    void shouldRejectBlankCurrency() {
        assertInvalidCommand(new RegisterAccountCommand(
                "user@example.com",
                "Password123!",
                " "
        ));
    }

    @Test
    void shouldRejectNullCurrency() {
        assertInvalidCommand(new RegisterAccountCommand(
                "user@example.com",
                "Password123!",
                null
        ));
    }

    @Test
    void shouldRejectUnsupportedCurrency() {
        assertInvalidCommand(new RegisterAccountCommand(
                "user@example.com",
                "Password123!",
                "XXX"
        ));
    }

    private void assertInvalidCommand(RegisterAccountCommand command) {
        assertThrows(
                IllegalArgumentException.class,
                () -> registerAccountService.register(command)
        );

        verifyNoInteractions(
                userAccountPort,
                userCredentialsRepositoryPort,
                emailVerificationTokenRepositoryPort,
                passwordEncoderPort,
                secureTokenGeneratorPort,
                tokenHasherPort,
                eventPublisher
        );
    }
}
