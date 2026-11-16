package rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.authentication.application.port.out.PasswordResetTokenRepositoryPort;
import rostyslav.sharuiev.expenses.authentication.domain.model.PasswordResetToken;
import rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.mapper.PasswordResetTokenPersistenceMapper;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
@RequiredArgsConstructor
public class PasswordResetTokenPersistenceAdapter implements PasswordResetTokenRepositoryPort {

    private final SpringDataPasswordResetTokenRepository repository;
    private final PasswordResetTokenPersistenceMapper mapper;

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        Optional<PasswordResetTokenJpaEntity> existingEntity = repository.findById(token.getId());

        if (existingEntity.isPresent()) {
            PasswordResetTokenJpaEntity entity = existingEntity.get();

            mapper.updateEntity(token, entity);

            return mapper.toDomain(entity);
        }

        PasswordResetTokenJpaEntity entity = mapper.toEntity(token);

        PasswordResetTokenJpaEntity savedEntity = repository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHashForUpdate(String tokenHash) {
        return repository
                .findByTokenHashForUpdate(tokenHash)
                .map(mapper::toDomain);
    }

    @Override
    public void invalidateAllByUserId(UUID userId) {
        repository.invalidateAllByUserId(userId);
    }
}
