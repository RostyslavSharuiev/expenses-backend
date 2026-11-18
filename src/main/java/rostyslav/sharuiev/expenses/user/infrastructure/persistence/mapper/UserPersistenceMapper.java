package rostyslav.sharuiev.expenses.user.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import rostyslav.sharuiev.expenses.user.domain.model.User;
import rostyslav.sharuiev.expenses.user.infrastructure.persistence.entity.UserJpaEntity;

@Component
public class UserPersistenceMapper {

    public User toDomain(UserJpaEntity entity) {
        return User.reconstitute(
                entity.getId(),
                entity.getEmail(),
                entity.getEmailVerifiedAt(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getAvatarUrl(),
                entity.getDefaultCurrency(),
                entity.getDeletedAt()
        );
    }

    public UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.getId(),
                user.getEmail(),
                user.getEmailVerifiedAt(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatarUrl(),
                user.getDefaultCurrency(),
                user.getDeletedAt()
        );
    }

    public void updateEntity(
            User user,
            UserJpaEntity entity
    ) {
        UserJpaEntity source = toEntity(user);
        entity.updateFrom(source);
    }
}
