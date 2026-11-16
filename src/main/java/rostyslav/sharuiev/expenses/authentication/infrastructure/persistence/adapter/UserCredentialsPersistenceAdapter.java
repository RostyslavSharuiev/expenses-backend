package rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.authentication.application.port.out.UserCredentialsRepositoryPort;
import rostyslav.sharuiev.expenses.authentication.domain.model.UserCredentials;
import rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.entity.UserCredentialsJpaEntity;
import rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.mapper.UserCredentialsPersistenceMapper;
import rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.repository.SpringDataUserCredentialsRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
@RequiredArgsConstructor
public class UserCredentialsPersistenceAdapter implements UserCredentialsRepositoryPort {

    private final SpringDataUserCredentialsRepository repository;
    private final UserCredentialsPersistenceMapper mapper;

    @Override
    public UserCredentials save(UserCredentials credentials) {
        Optional<UserCredentialsJpaEntity> existingEntity = repository.findById(credentials.getId());

        if (existingEntity.isPresent()) {
            UserCredentialsJpaEntity entity = existingEntity.get();

            mapper.updateEntity(credentials, entity);

            return mapper.toDomain(entity);
        }

        UserCredentialsJpaEntity entity = mapper.toEntity(credentials);

        UserCredentialsJpaEntity savedEntity = repository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserCredentials> findByUserId(UUID userId) {
        return repository
                .findByUserId(userId)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        repository.deleteByUserId(userId);
    }
}
