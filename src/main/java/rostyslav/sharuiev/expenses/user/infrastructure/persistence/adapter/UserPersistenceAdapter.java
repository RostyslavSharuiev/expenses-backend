package rostyslav.sharuiev.expenses.user.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rostyslav.sharuiev.expenses.user.application.port.out.UserRepositoryPort;
import rostyslav.sharuiev.expenses.user.domain.model.User;
import rostyslav.sharuiev.expenses.user.infrastructure.persistence.entity.UserJpaEntity;
import rostyslav.sharuiev.expenses.user.infrastructure.persistence.mapper.UserPersistenceMapper;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;
    private final UserPersistenceMapper mapper;
    private final EntityManager entityManager;

    @Override
    public User save(User user) {
        Optional<UserJpaEntity> existingEntity = repository.findById(user.getId());

        UserJpaEntity entity;

        if (existingEntity.isPresent()) {
            entity = existingEntity.get();
            mapper.updateEntity(user, entity);
        } else {
            entity = mapper.toEntity(user);
            entityManager.persist(entity);
        }

        return mapper.toDomain(entity);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository
                .findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository
                .findByEmailIgnoreCase(email)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }
}
