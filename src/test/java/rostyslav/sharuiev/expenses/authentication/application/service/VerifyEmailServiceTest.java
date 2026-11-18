package rostyslav.sharuiev.expenses.authentication.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rostyslav.sharuiev.expenses.authentication.application.port.out.EmailVerificationTokenRepositoryPort;
import rostyslav.sharuiev.expenses.authentication.domain.model.EmailVerificationToken;
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
class VerifyEmailServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-17T10:00:00Z");
    private static final String RAW_TOKEN = "raw-verification-token";
    private static final String TOKEN_HASH = "hashed-verification-token";

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private EmailVerificationTokenRepositoryPort tokenRepositoryPort;

    @Mock
    private TokenHasherPort tokenHasherPort;

    @Mock
    private UserAccountPort userAccountPort;

    private VerifyEmailService verifyEmailService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

        verifyEmailService = new VerifyEmailService(
                tokenRepositoryPort,
                tokenHasherPort,
                userAccountPort,
                clock
        );
    }

    @Test
    void shouldVerifyEmailSuccessfully() {
        EmailVerificationToken token = createToken(NOW.plus(Duration.ofHours(24)));

        UserAccountPort.UserAccount user = new UserAccountPort.UserAccount(
                USER_ID,
                "user@example.com",
                null,
                false
        );

        when(tokenHasherPort.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);
        when(tokenRepositoryPort.findByTokenHashForUpdate(TOKEN_HASH)).thenReturn(Optional.of(token));
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.of(user));

        verifyEmailService.verifyEmail(RAW_TOKEN);

        verify(tokenRepositoryPort).save(token);
        verify(userAccountPort).verifyEmail(USER_ID, NOW);

        // The token must be marked as used at the current time.
        assertEquals(NOW, token.getUsedAt());
    }

    @Test
    void shouldRejectUnknownToken() {
        when(tokenHasherPort.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);
        when(tokenRepositoryPort.findByTokenHashForUpdate(TOKEN_HASH)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> verifyEmailService.verifyEmail(RAW_TOKEN)
        );

        verify(userAccountPort, never()).findById(any());
        verify(tokenRepositoryPort, never()).save(any());
        verify(userAccountPort, never()).verifyEmail(any(), any());
    }

    @Test
    void shouldRejectExpiredToken() {
        EmailVerificationToken token = createToken(NOW.minusSeconds(1));

        when(tokenHasherPort.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);
        when(tokenRepositoryPort.findByTokenHashForUpdate(TOKEN_HASH)).thenReturn(Optional.of(token));

        assertThrows(
                IllegalArgumentException.class,
                () -> verifyEmailService.verifyEmail(RAW_TOKEN)
        );

        verify(userAccountPort, never()).findById(any());
        verify(tokenRepositoryPort, never()).save(any());
        verify(userAccountPort, never()).verifyEmail(any(), any());
    }

    @Test
    void shouldRejectAlreadyUsedToken() {
        EmailVerificationToken token = createToken(NOW.plus(Duration.ofHours(24)));
        token.markAsUsed(NOW.minusSeconds(10));

        when(tokenHasherPort.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);
        when(tokenRepositoryPort.findByTokenHashForUpdate(TOKEN_HASH)).thenReturn(Optional.of(token));

        assertThrows(
                IllegalArgumentException.class,
                () -> verifyEmailService.verifyEmail(RAW_TOKEN)
        );

        verify(userAccountPort, never()).findById(any());
        verify(tokenRepositoryPort, never()).save(any());
        verify(userAccountPort, never()).verifyEmail(any(), any());
    }

    @Test
    void shouldRejectDeletedUser() {
        EmailVerificationToken token = createToken(NOW.plus(Duration.ofHours(24)));

        UserAccountPort.UserAccount user = new UserAccountPort.UserAccount(
                USER_ID,
                "user@example.com",
                null,
                true
        );

        when(tokenHasherPort.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);
        when(tokenRepositoryPort.findByTokenHashForUpdate(TOKEN_HASH)).thenReturn(Optional.of(token));
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThrows(
                IllegalArgumentException.class,
                () -> verifyEmailService.verifyEmail(RAW_TOKEN)
        );

        verify(tokenRepositoryPort, never()).save(any());
        verify(userAccountPort, never()).verifyEmail(any(), any());
    }

    @Test
    void shouldRejectAlreadyVerifiedUser() {
        EmailVerificationToken token = createToken(NOW.plus(Duration.ofHours(24)));

        UserAccountPort.UserAccount user = new UserAccountPort.UserAccount(
                USER_ID,
                "user@example.com",
                NOW.minus(Duration.ofDays(1)),
                false
        );

        when(tokenHasherPort.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);
        when(tokenRepositoryPort.findByTokenHashForUpdate(TOKEN_HASH)).thenReturn(Optional.of(token));
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThrows(
                IllegalArgumentException.class,
                () -> verifyEmailService.verifyEmail(RAW_TOKEN)
        );

        verify(tokenRepositoryPort, never()).save(any());
        verify(userAccountPort, never()).verifyEmail(any(), any());
    }

    @Test
    void shouldRejectWhenUserDoesNotExist() {
        EmailVerificationToken token = createToken(NOW.plus(Duration.ofHours(24)));

        when(tokenHasherPort.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);
        when(tokenRepositoryPort.findByTokenHashForUpdate(TOKEN_HASH)).thenReturn(Optional.of(token));
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> verifyEmailService.verifyEmail(RAW_TOKEN)
        );

        verify(tokenRepositoryPort, never()).save(any());
        verify(userAccountPort, never()).verifyEmail(any(), any());
    }

    private EmailVerificationToken createToken(Instant expiresAt) {
        return EmailVerificationToken.create(
                USER_ID,
                TOKEN_HASH,
                expiresAt,
                NOW.minus(Duration.ofHours(2))
        );
    }
}