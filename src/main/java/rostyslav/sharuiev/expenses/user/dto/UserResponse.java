package rostyslav.sharuiev.expenses.user.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,

        String email,

        boolean emailVerified,

        String firstName,

        String lastName,

        String avatarUrl,

        Instant createdAt
) {
}
