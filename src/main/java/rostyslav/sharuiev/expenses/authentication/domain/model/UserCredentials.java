package rostyslav.sharuiev.expenses.authentication.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class UserCredentials {

    private final UUID id;
    private final UUID userId;

    private String passwordHash;
    private Instant passwordChangedAt;

    private UserCredentials(
            UUID id,
            UUID userId,
            String passwordHash,
            Instant passwordChangedAt
    ) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.passwordHash = validatePasswordHash(passwordHash);
        this.passwordChangedAt = Objects.requireNonNull(passwordChangedAt, "passwordChangedAt cannot be null");
    }

    public static UserCredentials create(
            UUID userId,
            String passwordHash,
            Instant passwordChangedAt
    ) {
        return new UserCredentials(
                UUID.randomUUID(),
                userId,
                passwordHash,
                passwordChangedAt
        );
    }

    public static UserCredentials reconstitute(
            UUID id,
            UUID userId,
            String passwordHash,
            Instant passwordChangedAt
    ) {
        return new UserCredentials(id, userId, passwordHash, passwordChangedAt);
    }

    public void changePassword(String newPasswordHash, Instant passwordChangedAt) {
        this.passwordHash = validatePasswordHash(newPasswordHash);
        this.passwordChangedAt = Objects.requireNonNull(passwordChangedAt, "passwordChangedAt cannot be null");
    }

    private static String validatePasswordHash(String passwordHash) {
        Objects.requireNonNull(passwordHash, "passwordHash cannot be null");

        if (passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash cannot be blank");
        }

        return passwordHash;
    }
}
