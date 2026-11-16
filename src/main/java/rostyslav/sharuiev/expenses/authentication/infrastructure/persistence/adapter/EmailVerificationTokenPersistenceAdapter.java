package rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.authentication.application.port.out.EmailVerificationTokenRepositoryPort;
import rostyslav.sharuiev.expenses.authentication.domain.model.EmailVerificationToken;
import rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.entity.EmailVerificationTokenJpaEntity;
import rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.mapper.EmailVerificationTokenPersistenceMapper;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
@RequiredArgsConstructor
public class EmailVerificationTokenPersistenceAdapter implements EmailVerificationTokenRepositoryPort {

    private final SpringDataEmailVerificationTokenRepository repository;
    private final EmailVerificationTokenPersistenceMapper mapper;

    @Override
    public EmailVerificationToken save(EmailVerificationToken token) {
        Optional<EmailVerificationTokenJpaEntity> existingEntity = repository.findById(token.getId());

        if (existingEntity.isPresent()) {
            EmailVerificationTokenJpaEntity entity = existingEntity.get();

            mapper.updateEntity(token, entity);

            return mapper.toDomain(entity);
        }

        EmailVerificationTokenJpaEntity entity = mapper.toEntity(token);

        EmailVerificationTokenJpaEntity savedEntity = repository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<EmailVerificationToken> findByTokenHashForUpdate(String tokenHash) {
        return repository
                .findByTokenHashForUpdate(tokenHash)
                .map(mapper::toDomain);
    }

    @Override
    public void invalidateAllByUserId(UUID userId) {
        repository.invalidateAllByUserId(userId);
    }
}
