package rostyslav.sharuiev.expenses.authentication.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.authentication.application.port.in.ResetPasswordUseCase;
import rostyslav.sharuiev.expenses.authentication.application.port.out.*;
import rostyslav.sharuiev.expenses.authentication.domain.model.PasswordResetToken;
import rostyslav.sharuiev.expenses.authentication.domain.model.UserCredentials;
import rostyslav.sharuiev.expenses.session.application.port.in.SessionManagementUseCase;
import rostyslav.sharuiev.expenses.user.application.port.in.UserAccountPort;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPasswordUseCase {

    private final PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;
    private final UserCredentialsRepositoryPort userCredentialsRepositoryPort;
    private final UserAccountPort userAccountPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenHasherPort tokenHasherPort;
    private final SessionManagementUseCase sessionManagementUseCase;
    private final Clock clock;

    @Override
    public ResetPasswordResult resetPassword(ResetPasswordCommand command) {
        validateCommand(command);

        Instant now = clock.instant();

        String tokenHash = tokenHasherPort.hash(command.rawToken());

        PasswordResetToken token = passwordResetTokenRepositoryPort
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token"));

        if (!token.isUsableAt(now)) {
            throw new IllegalArgumentException("Invalid or expired password reset token");
        }

        UUID userId = token.getUserId();

        UserAccountPort.UserAccount user = userAccountPort
                .findById(userId)
                .filter(account -> !account.deleted())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token"));

        UserCredentials credentials = userCredentialsRepositoryPort
                .findByUserId(user.id())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token"));

        String newPasswordHash = passwordEncoderPort.encode(command.newPassword());

        credentials.changePassword(newPasswordHash, now);
        userCredentialsRepositoryPort.save(credentials);

        token.markAsUsed(now);
        passwordResetTokenRepositoryPort.save(token);
        passwordResetTokenRepositoryPort.invalidateAllByUserId(userId);

        sessionManagementUseCase.revokeAllSessions(userId);

        SessionManagementUseCase.SessionsTokens sessionsTokens = sessionManagementUseCase.createSession(userId);

        return new ResetPasswordResult(
                sessionsTokens.accessToken(),
                sessionsTokens.refreshToken(),
                sessionsTokens.accessTokenExpiresIn()
        );
    }

    private void validateCommand(ResetPasswordCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command must not be null");
        }

        if (command.rawToken() == null || command.rawToken().isBlank()) {
            throw new IllegalArgumentException("Reset token must not be blank");
        }

        if (command.newPassword() == null || command.newPassword().isBlank()) {
            throw new IllegalArgumentException("New password must not be blank");
        }
    }
}
