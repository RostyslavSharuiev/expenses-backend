package rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import rostyslav.sharuiev.expenses.authentication.domain.model.OAuthAccount;
import rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.entity.OAuthAccountJpaEntity;

@Component
public class OAuthAccountPersistenceMapper {

    public OAuthAccount toDomain(OAuthAccountJpaEntity entity) {
        return OAuthAccount.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getProvider(),
                entity.getProviderUserId(),
                entity.getProviderEmail(),
                entity.getProviderEmailVerified(),
                entity.getLinkedAt()
        );
    }

    public OAuthAccountJpaEntity toEntity(OAuthAccount account) {
        return new OAuthAccountJpaEntity(
                account.getId(),
                account.getUserId(),
                account.getProvider(),
                account.getProviderUserId(),
                account.getProviderEmail(),
                account.getProviderEmailVerified(),
                account.getLinkedAt()
        );
    }

    public void updateEntity(
            OAuthAccount account,
            OAuthAccountJpaEntity entity
    ) {
        OAuthAccountJpaEntity source = toEntity(account);
        entity.updateProviderProfile(source);
    }
}
