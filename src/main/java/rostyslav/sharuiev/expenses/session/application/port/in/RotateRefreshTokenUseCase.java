package rostyslav.sharuiev.expenses.session.application.port.in;

import java.util.UUID;

public interface RotateRefreshTokenUseCase {

    RotationResult rotateRefreshToken(String rawRefreshToken);

    record RotationResult(
            UUID userId,
            String refreshToken
    ) {}
}
