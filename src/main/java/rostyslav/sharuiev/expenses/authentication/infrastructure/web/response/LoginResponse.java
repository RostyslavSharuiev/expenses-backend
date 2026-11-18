package rostyslav.sharuiev.expenses.authentication.infrastructure.web.response;

import java.util.UUID;

public record LoginResponse(
        UUID userId,
        String accessToken,
        long accessTokenExpiresIn
) {
}
