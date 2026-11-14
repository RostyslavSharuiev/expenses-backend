package rostyslav.sharuiev.expenses.authentication.domain.model;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class UserCredentials {

    private final UUID id;
    private final UUID userId;

    private String passwordHash;

    private UserCredentials(
            UUID id,
            UUID userId,
            String passwordHash
    ) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.passwordHash = validatePasswordHash(passwordHash);
    }

    public static UserCredentials create(
            UUID userId,
            String passwordHash
    ) {
        return new UserCredentials(
                UUID.randomUUID(),
                userId,
                passwordHash
        );
    }

    public static UserCredentials reconstitute(
            UUID id,
            UUID userId,
            String passwordHash
    ) {
        return new UserCredentials(id, userId, passwordHash);
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = validatePasswordHash(newPasswordHash);
    }

    private static String validatePasswordHash(String passwordHash) {
        Objects.requireNonNull(passwordHash, "passwordHash cannot be null");

        if (passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash cannot be blank");
        }

        return passwordHash;
    }
}
