package rostyslav.sharuiev.expenses.authentication.application.out;

import rostyslav.sharuiev.expenses.authentication.domain.enums.OAuthProvider;
import rostyslav.sharuiev.expenses.authentication.domain.model.OAuthAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OAuthAccountRepositoryPort {

    OAuthAccount save(OAuthAccount account);

    Optional<OAuthAccount> findByProviderAndProviderUserId(
            OAuthProvider provider,
            String providerUserId
    );

    Optional<OAuthAccount> findByUserIdAndProvider(
            UUID userId,
            OAuthProvider provider
    );

    List<OAuthAccount> findAllByUserId(UUID userId);

    // TODO: check later
//    boolean existsByUserIdAndProvider(
//            UUID userId,
//            OAuthProvider provider
//    );
}
