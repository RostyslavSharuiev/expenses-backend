package rostyslav.sharuiev.expenses.authentication.infrastructure.web.response;

import java.util.UUID;

public record RegisterAccountResponse(
        UUID userId,
        String email
) {
}
