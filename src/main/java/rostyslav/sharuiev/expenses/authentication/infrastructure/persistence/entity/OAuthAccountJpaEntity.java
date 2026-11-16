package rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import rostyslav.sharuiev.expenses.authentication.domain.enums.OAuthProvider;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "oauth_accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_oauth_accounts_user_provider",
                        columnNames = {"user_id", "provider"}
                ),
                @UniqueConstraint(
                        name = "uk_oauth_accounts_user_id",
                        columnNames = {"provider", "provider_user_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthAccountJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private OAuthProvider provider;

    @Column(nullable = false, updatable = false)
    private String providerUserId;

    @Column
    private String providerEmail;

    @Column
    private Boolean providerEmailVerified;

    @Column(nullable = false, updatable = false)
    private Instant linkedAt;

    public OAuthAccountJpaEntity(
            UUID id,
            UUID userId,
            OAuthProvider provider,
            String providerUserId,
            String providerEmail,
            Boolean providerEmailVerified,
            Instant linkedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.providerEmail = providerEmail;
        this.providerEmailVerified = providerEmailVerified;
        this.linkedAt = linkedAt;
    }

    public void updateProviderProfile(OAuthAccountJpaEntity source) {
        this.providerEmail = source.providerEmail;
        this.providerEmailVerified = source.providerEmailVerified;
    }
}
