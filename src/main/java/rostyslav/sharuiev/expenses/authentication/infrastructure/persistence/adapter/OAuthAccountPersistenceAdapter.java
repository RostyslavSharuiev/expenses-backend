package rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.authentication.application.out.OAuthAccountRepositoryPort;
import rostyslav.sharuiev.expenses.authentication.domain.enums.OAuthProvider;
import rostyslav.sharuiev.expenses.authentication.domain.model.OAuthAccount;
import rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.entity.OAuthAccountJpaEntity;
import rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.mapper.OAuthAccountPersistenceMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
@RequiredArgsConstructor
public class OAuthAccountPersistenceAdapter implements OAuthAccountRepositoryPort {

    private final SpringDataOAuthAccountRepository repository;
    private final OAuthAccountPersistenceMapper mapper;

    @Override
    public OAuthAccount save(OAuthAccount account) {
        Optional<OAuthAccountJpaEntity> existingEntity = repository.findById(account.getId());

        if (existingEntity.isPresent()) {
            OAuthAccountJpaEntity entity = existingEntity.get();

            mapper.updateEntity(account, entity);

            return mapper.toDomain(entity);
        }

        OAuthAccountJpaEntity entity = mapper.toEntity(account);

        OAuthAccountJpaEntity savedEntity = repository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OAuthAccount> findByProviderAndProviderUserId(
            OAuthProvider provider,
            String providerUserId
    ) {
        return repository
                .findByProviderAndProviderUserId(provider, providerUserId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OAuthAccount> findByUserIdAndProvider(
            UUID userId,
            OAuthProvider provider
    ) {
        return repository
                .findByUserIdAndProvider(userId, provider)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OAuthAccount> findAllByUserId(UUID userId) {
        return repository
                .findAllByUserId(userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
