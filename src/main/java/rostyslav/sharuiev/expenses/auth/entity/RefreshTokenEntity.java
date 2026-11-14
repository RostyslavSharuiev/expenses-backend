package rostyslav.sharuiev.expenses.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import rostyslav.sharuiev.expenses.user.entity.UserEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_refresh_token_user", columnList = "user_id"),
                @Index(name = "idx_refresh_token_family", columnList = "token_family_id")
        }
)
@Setter
@Getter
@NoArgsConstructor
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, unique = true)
    private String tokenHash;

    @Column(nullable = false)
    private UUID tokenFamilyId;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant revokedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
