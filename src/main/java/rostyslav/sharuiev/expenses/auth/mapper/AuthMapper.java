package rostyslav.sharuiev.expenses.auth.mapper;

import org.springframework.stereotype.Component;
import rostyslav.sharuiev.expenses.auth.dto.RegisterRequest;
import rostyslav.sharuiev.expenses.user.entity.UserCredentialsEntity;
import rostyslav.sharuiev.expenses.user.entity.UserEntity;

import java.time.Instant;

@Component
public class AuthMapper {

    public UserEntity toUserEntity(RegisterRequest request, String normalizedEmail, boolean emailVerified) {
        var user = new UserEntity();

        user.setEmail(normalizedEmail);
        user.setEmailVerified(emailVerified);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        return user;
    }

    public UserCredentialsEntity toUserCredentialsEntity(
            UserEntity user,
            String passwordHash,
            Instant passwordChangedAt
    ) {
        var credentials = new UserCredentialsEntity();

        credentials.setUser(user);
        credentials.setPasswordHash(passwordHash);
        credentials.setPasswordChangedAt(passwordChangedAt);

        return credentials;
    }
}
