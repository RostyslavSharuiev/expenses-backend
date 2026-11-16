package rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import rostyslav.sharuiev.expenses.authentication.domain.model.PasswordResetToken;
import rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;

@Component
public class PasswordResetTokenPersistenceMapper {

    public PasswordResetToken toDomain(PasswordResetTokenJpaEntity entity) {
        return PasswordResetToken.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getUsedAt(),
                entity.getInvalidatedAt()
        );
    }

    public PasswordResetTokenJpaEntity toEntity(PasswordResetToken token) {
        return new PasswordResetTokenJpaEntity(
                token.getId(),
                token.getUserId(),
                token.getTokenHash(),
                token.getExpiresAt(),
                token.getUsedAt(),
                token.getInvalidatedAt()
        );
    }

    public void updateEntity(
            PasswordResetToken token,
            PasswordResetTokenJpaEntity entity
    ) {
        PasswordResetTokenJpaEntity source = toEntity(token);
        entity.updateFrom(source);
    }
}
