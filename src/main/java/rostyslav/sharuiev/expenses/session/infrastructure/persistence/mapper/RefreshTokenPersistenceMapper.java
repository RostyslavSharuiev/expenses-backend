package rostyslav.sharuiev.expenses.session.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import rostyslav.sharuiev.expenses.session.domain.model.RefreshToken;
import rostyslav.sharuiev.expenses.session.infrastructure.persistence.entity.RefreshTokenJpaEntity;

@Component
public class RefreshTokenPersistenceMapper {

    public RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getSessionId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getRevokedAt(),
                entity.getReplacedByTokenId()
        );
    }

    public RefreshTokenJpaEntity toEntity(RefreshToken refreshToken) {
        return new RefreshTokenJpaEntity(
                refreshToken.getId(),
                refreshToken.getUserId(),
                refreshToken.getSessionId(),
                refreshToken.getTokenHash(),
                refreshToken.getExpiresAt()
        );
    }

    public void updateEntity(
            RefreshToken refreshToken,
            RefreshTokenJpaEntity entity
    ) {
        RefreshTokenJpaEntity source = toEntity(refreshToken);
        entity.updateFrom(source);
    }
}
