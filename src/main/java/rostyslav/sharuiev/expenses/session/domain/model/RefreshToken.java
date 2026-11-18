package rostyslav.sharuiev.expenses.session.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class RefreshToken {

    private final UUID id;
    private final UUID userId;
    private final UUID sessionId;
    private final String tokenHash;
    private final Instant expiresAt;
    private final Instant createdAt;

    private Instant revokedAt;
    private UUID replacedByTokenId;

    private RefreshToken(
            UUID id,
            UUID userId,
            UUID sessionId,
            String tokenHash,
            Instant expiresAt,
            Instant createdAt,
            Instant revokedAt,
            UUID replacedByTokenId
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId must not be null");

        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("tokenHash must not be blank");
        }

        this.tokenHash = tokenHash;
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");

        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("Expiration time must be after creation time");
        }

        if (revokedAt != null && revokedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("Revocation time must not be before creation time");
        }

        if (replacedByTokenId != null && revokedAt == null) {
            throw new IllegalArgumentException("A replaced token must be revoked");
        }

        if (id.equals(replacedByTokenId)) {
            throw new IllegalArgumentException("Replacement token must be different");
        }

        this.revokedAt = revokedAt;
        this.replacedByTokenId = replacedByTokenId;
    }

    public static RefreshToken create(
            UUID userId,
            UUID sessionId,
            String tokenHash,
            Instant expiresAt,
            Instant createdAt
    ) {
        return new RefreshToken(
                UUID.randomUUID(),
                userId,
                sessionId,
                tokenHash,
                expiresAt,
                createdAt,
                null,
                null
        );
    }

    public static RefreshToken reconstitute(
            UUID id,
            UUID userId,
            UUID sessionId,
            String tokenHash,
            Instant expiresAt,
            Instant createdAt,
            Instant revokedAt,
            UUID replacedByTokenId
    ) {
        return new RefreshToken(
                id,
                userId,
                sessionId,
                tokenHash,
                expiresAt,
                createdAt,
                revokedAt,
                replacedByTokenId
        );
    }

    public void revoke(Instant revokedAt) {
        Objects.requireNonNull(revokedAt, "revokedAt must not be null");

        if (isRevoked()) {
            throw new IllegalStateException("Refresh token is already revoked");
        }

        if (revokedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("Revocation time must not be before creation time");
        }

        this.revokedAt = revokedAt;
    }

    public void replaceWith(UUID replacementTokenId, Instant now) {
        Objects.requireNonNull(replacementTokenId, "replacementTokenId must not be null");
        Objects.requireNonNull(now, "now must not be null");

        if (replacementTokenId.equals(id)) {
            throw new IllegalArgumentException("Replacement token must be different");
        }

        if (isReplaced()) {
            throw new IllegalStateException("Refresh token is already replaced");
        }

        if (!isUsableAt(now)) {
            throw new IllegalStateException("Refresh token is not usable");
        }

        revoke(now);
        this.replacedByTokenId = replacementTokenId;
    }

    public boolean isExpired(Instant now) {
        Objects.requireNonNull(now, "now must not be null");

        return !expiresAt.isAfter(now);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isUsableAt(Instant now) {
        return !isRevoked() && !isExpired(now);
    }

    public boolean isReplaced() {
        return replacedByTokenId != null;
    }
}
