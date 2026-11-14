package rostyslav.sharuiev.expenses.authentication.application.out;

import rostyslav.sharuiev.expenses.authentication.domain.model.UserCredentials;

import java.util.Optional;
import java.util.UUID;

public interface UserCredentialsRepositoryPort {

    UserCredentials save(UserCredentials credentials);

    Optional<UserCredentials> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
