package rostyslav.sharuiev.expenses.authentication.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class EmailVerificationToken {

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final Instant expiresAt;
    private final Instant createdAt;

    private Instant usedAt;

    private EmailVerificationToken(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            Instant createdAt,
            Instant usedAt
    ) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.tokenHash = validateTokenHash(tokenHash);
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.usedAt = usedAt;

        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("expiresAt must be after createdAt");
        }

        if (usedAt != null && usedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("usedAt cannot be before createdAt");
        }
    }

    public static EmailVerificationToken create(
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            Instant createdAt
    ) {
        return new EmailVerificationToken(
                UUID.randomUUID(),
                userId,
                tokenHash,
                expiresAt,
                createdAt,
                null
        );
    }

    public static EmailVerificationToken reconstitute(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            Instant createdAt,
            Instant usedAt
    ) {
        return new EmailVerificationToken(
                id,
                userId,
                tokenHash,
                expiresAt,
                createdAt,
                usedAt
        );
    }

    public boolean isExpired(Instant now) {
        Objects.requireNonNull(now, "now cannot be null");

        return !expiresAt.isAfter(now);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isUsableAt(Instant now) {
        return !isUsed() && !isExpired(now);
    }

    public void markAsUsed(Instant now) {
        Objects.requireNonNull(now, "now cannot be null");

        if (!isUsableAt(now)) {
            throw new IllegalStateException("Email verification token is no longer usable");
        }

        this.usedAt = now;
    }

    private static String validateTokenHash(String tokenHash) {
        Objects.requireNonNull(tokenHash, "tokenHash cannot be null");

        if (tokenHash.isBlank()) {
            throw new IllegalArgumentException("tokenHash cannot be blank");
        }

        return tokenHash;
    }
}
