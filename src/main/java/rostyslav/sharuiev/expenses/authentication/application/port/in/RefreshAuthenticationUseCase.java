package rostyslav.sharuiev.expenses.authentication.application.port.in;

import java.util.UUID;

public interface RefreshAuthenticationUseCase {

    RefreshAuthenticationResult refresh(String rawRefreshToken);

    record RefreshAuthenticationResult(
            UUID userId,
            String accessToken,
            String refreshToken,
            long accessTokenExpiresIn
    ) {}
}
