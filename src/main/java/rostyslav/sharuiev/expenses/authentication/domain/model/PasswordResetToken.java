package rostyslav.sharuiev.expenses.authentication.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class PasswordResetToken {

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final Instant expiresAt;
    private final Instant createdAt;

    private Instant usedAt;
    private Instant invalidatedAt;

    private PasswordResetToken(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            Instant createdAt,
            Instant usedAt,
            Instant invalidatedAt
    ) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.tokenHash = validateTokenHash(tokenHash);
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");

        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("expiresAt must be after createdAt");
        }

        validateStateTimestamp(usedAt, "usedAt", createdAt);
        validateStateTimestamp(invalidatedAt, "invalidatedAt", createdAt);

        if (usedAt != null && invalidatedAt != null) {
            throw new IllegalArgumentException("Token cannot be both used and invalidated");
        }

        this.usedAt = usedAt;
        this.invalidatedAt = invalidatedAt;

    }

    public static PasswordResetToken create(
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            Instant createdAt
    ) {
        return new PasswordResetToken(
                UUID.randomUUID(),
                userId,
                tokenHash,
                expiresAt,
                createdAt,
                null,
                null
        );
    }

    public static PasswordResetToken reconstitute(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            Instant createdAt,
            Instant usedAt,
            Instant invalidatedAt
    ) {
        return new PasswordResetToken(
                id,
                userId,
                tokenHash,
                expiresAt,
                createdAt,
                usedAt,
                invalidatedAt
        );
    }

    public boolean isExpired(Instant now) {
        Objects.requireNonNull(now, "now cannot be null");

        return !expiresAt.isAfter(now);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isInvalidated() {
        return invalidatedAt != null;
    }

    public boolean isUsableAt(Instant now) {
        Objects.requireNonNull(now, "now cannot be null");

        return !isUsed()
                && !isInvalidated()
                && !isExpired(now);
    }

    public void markAsUsed(Instant now) {
        validateTransitionTimestamp(now);

        if (isUsed()) {
            throw new IllegalStateException("Token has already been used");
        }

        if (isInvalidated()) {
            throw new IllegalStateException("Token has been invalidated");
        }

        if (isExpired(now)) {
            throw new IllegalStateException("Token has expired");
        }

        this.usedAt = now;
    }

    public void invalidate(Instant now) {
        validateTransitionTimestamp(now);

        if (isUsed()) {
            throw new IllegalStateException("Used token cannot be invalidated");
        }

        if (isInvalidated()) {
            throw new IllegalStateException("Token has already been invalidated");
        }

        this.invalidatedAt = now;
    }

    private String validateTokenHash(String tokenHash) {
        Objects.requireNonNull(tokenHash, "tokenHash cannot be null");

        if (tokenHash.isBlank()) {
            throw new IllegalArgumentException("tokenHash cannot be blank");
        }

        return tokenHash;
    }

    private static void validateStateTimestamp(
            Instant timestamp,
            String fieldName,
            Instant createdAt
    ) {
        if (timestamp != null && timestamp.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    String.format("%s cannot be before createdAt", fieldName)
            );
        }
    }

    private void validateTransitionTimestamp(Instant now) {
        Objects.requireNonNull(now, "now cannot be null");

        if (now.isBefore(createdAt)) {
            throw new IllegalArgumentException("Transition timestamp cannot be before createdAt");
        }
    }
}
