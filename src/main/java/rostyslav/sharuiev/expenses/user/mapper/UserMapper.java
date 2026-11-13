package rostyslav.sharuiev.expenses.user.mapper;

import org.springframework.stereotype.Component;
import rostyslav.sharuiev.expenses.user.dto.CreateUserRequest;
import rostyslav.sharuiev.expenses.user.dto.UserResponse;
import rostyslav.sharuiev.expenses.user.entity.UserEntity;

@Component
public class UserMapper {

    public UserEntity toEntity(CreateUserRequest request) {
        var user = new UserEntity();

        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        return user;
    }

    public UserResponse toResponse(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.isEmailVerified(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatarUrl(),
                user.getCreatedAt()
        );
    }
}
