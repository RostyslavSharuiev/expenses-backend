package rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rostyslav.sharuiev.expenses.authentication.domain.enums.OAuthProvider;
import rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.entity.OAuthAccountJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataOAuthAccountRepository extends JpaRepository<OAuthAccountJpaEntity, UUID> {

    Optional<OAuthAccountJpaEntity> findByProviderAndProviderUserId(
            OAuthProvider provider,
            String providerUserId
    );

    Optional<OAuthAccountJpaEntity> findByUserIdAndProvider(
            UUID userId,
            OAuthProvider provider
    );

    List<OAuthAccountJpaEntity> findAllByUserId(UUID userId);
}
