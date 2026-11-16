package rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import rostyslav.sharuiev.expenses.authentication.domain.model.EmailVerificationToken;
import rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.entity.EmailVerificationTokenJpaEntity;

@Component
public class EmailVerificationTokenPersistenceMapper {

    public EmailVerificationToken toDomain(EmailVerificationTokenJpaEntity entity) {
        return EmailVerificationToken.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getUsedAt(),
                entity.getInvalidatedAt()
        );
    }

    public EmailVerificationTokenJpaEntity toEntity(EmailVerificationToken token) {
        return new EmailVerificationTokenJpaEntity(
                token.getId(),
                token.getUserId(),
                token.getTokenHash(),
                token.getExpiresAt(),
                token.getUsedAt(),
                token.getInvalidatedAt()
        );
    }

    public void updateEntity(
            EmailVerificationToken token,
            EmailVerificationTokenJpaEntity entity
    ) {
        EmailVerificationTokenJpaEntity source = toEntity(token);
        entity.updateFrom(source);
    }
}
