package rostyslav.sharuiev.expenses.session.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.session.application.port.out.RefreshTokenRepositoryPort;
import rostyslav.sharuiev.expenses.session.domain.model.RefreshToken;
import rostyslav.sharuiev.expenses.session.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import rostyslav.sharuiev.expenses.session.infrastructure.persistence.mapper.RefreshTokenPersistenceMapper;
import rostyslav.sharuiev.expenses.session.infrastructure.persistence.repository.SpringDataRefreshTokenRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
@RequiredArgsConstructor
public class RefreshTokenPersistenceAdapter implements RefreshTokenRepositoryPort {

    private final SpringDataRefreshTokenRepository repository;
    private final RefreshTokenPersistenceMapper mapper;
    private final Clock clock;

    @Override
    public RefreshToken save(RefreshToken token) {
        Optional<RefreshTokenJpaEntity> existingEntity = repository.findById(token.getId());

        if (existingEntity.isPresent()) {
            RefreshTokenJpaEntity entity = existingEntity.get();

            mapper.updateEntity(token, entity);

            return mapper.toDomain(entity);
        }

        RefreshTokenJpaEntity entity = mapper.toEntity(token);

        RefreshTokenJpaEntity savedEntity = repository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findById(UUID id) {
        return repository
                .findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<RefreshToken> findByTokenHashForUpdate(String tokenHash) {
        return repository
                .findByTokenHashForUpdate(tokenHash)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> findAllBySessionId(UUID sessionId) {
        return repository
                .findAllBySessionId(sessionId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void revokeAllBySessionId(UUID sessionId) {
        Instant revokedAt = clock.instant();

        List<RefreshTokenJpaEntity> entities = repository.findAllBySessionIdForUpdate(sessionId);

        entities.forEach(entity -> {
            RefreshToken token = mapper.toDomain(entity);

            if (!token.isRevoked()) {
                token.revoke(revokedAt);
                mapper.updateEntity(token, entity);
            }
        });
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        Instant revokedAt = clock.instant();

        List<RefreshTokenJpaEntity> entities = repository.findAllByUserIdForUpdate(userId);

        entities.forEach(entity -> {
            RefreshToken token = mapper.toDomain(entity);

            if (!token.isRevoked()) {
                token.revoke(revokedAt);
                mapper.updateEntity(token, entity);
            }
        });
    }
}
