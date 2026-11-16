package rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "password_reset_tokens",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_password_reset_tokens_token_hash",
                        columnNames = "token_hash"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetTokenJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, updatable = false)
    private String tokenHash;

    @Column(nullable = false, updatable = false)
    private Instant expiresAt;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant usedAt;

    @Column
    private Instant invalidatedAt;

    public PasswordResetTokenJpaEntity(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            Instant usedAt,
            Instant invalidatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.invalidatedAt = invalidatedAt;
    }

    public void updateFrom(PasswordResetTokenJpaEntity source) {
        this.usedAt = source.usedAt;
        this.invalidatedAt = source.invalidatedAt;
    }
}
