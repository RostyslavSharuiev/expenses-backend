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
        UserJpaEntity entity = new UserJpaEntity();

        updateEntity(user, entity);

        return entity;
    }

    public void updateEntity(
            User user,
            UserJpaEntity entity
    ) {
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setEmailVerifiedAt(user.getEmailVerifiedAt());

        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setAvatarUrl(user.getAvatarUrl());

        entity.setDefaultCurrency(user.getDefaultCurrency());
        entity.setDeletedAt(user.getDeletedAt());
    }
}
