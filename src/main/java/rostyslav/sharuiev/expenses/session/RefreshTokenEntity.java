package rostyslav.sharuiev.expenses.session;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "refresh_tokens",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_refresh_tokens_token_hash",
                        columnNames = "token_hash"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, updatable = false)
    private UUID sessionId;

    @Column(nullable = false, updatable = false)
    private String tokenHash;

    @Column(nullable = false, updatable = false)
    private Instant expiresAt;

    @Column
    private Instant revokedAt;

    @Column
    private UUID replacedByTokenId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private RefreshTokenEntity(
            UUID userId,
            UUID sessionId,
            String tokenHash,
            Instant expiresAt
    ) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public static RefreshTokenEntity create(
            UUID userId,
            UUID sessionId,
            String tokenHash,
            Instant expiresAt
    ) {
        return new RefreshTokenEntity(userId, sessionId, tokenHash, expiresAt);
    }

    public void revoke(Instant revokedAt) {
        if (this.revokedAt != null) {
            return;
        }

        this.revokedAt = revokedAt;
    }

    public void replaceWith(UUID replacementTokenId, Instant revokedAt) {
        if (this.revokedAt != null) {
            throw new IllegalArgumentException("Refresh token has already been revoked");
        }

        this.revokedAt = revokedAt;
        this.replacedByTokenId = replacementTokenId;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }
}
