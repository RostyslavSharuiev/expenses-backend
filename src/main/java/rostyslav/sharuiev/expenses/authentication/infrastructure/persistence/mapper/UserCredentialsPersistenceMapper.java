package rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import rostyslav.sharuiev.expenses.authentication.domain.model.UserCredentials;
import rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.entity.UserCredentialsJpaEntity;

@Component
public class UserCredentialsPersistenceMapper {

    public UserCredentials toDomain(UserCredentialsJpaEntity entity) {
        return UserCredentials.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getPasswordHash(),
                entity.getPasswordChangedAt()
        );
    }

    public UserCredentialsJpaEntity toEntity(UserCredentials credentials) {
        return new UserCredentialsJpaEntity(
                credentials.getId(),
                credentials.getUserId(),
                credentials.getPasswordHash(),
                credentials.getPasswordChangedAt()
        );
    }

    public void updateEntity(
            UserCredentials credentials,
            UserCredentialsJpaEntity entity
    ) {
        UserCredentialsJpaEntity source = toEntity(credentials);
        entity.updateFrom(source);
    }
}
