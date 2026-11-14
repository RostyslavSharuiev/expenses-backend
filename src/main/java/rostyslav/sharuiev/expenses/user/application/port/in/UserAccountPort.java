package rostyslav.sharuiev.expenses.user.application.port.in;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserAccountPort {

    UserAccount create(CreateUserCommand command);

    Optional<UserAccount> findById(UUID userId);

    Optional<UserAccount> findByEmail(String email);

    record UserAccount(
            UUID id,
            String email,
            Instant emailVerifiedAt,
            boolean deleted
    ) {}

    record CreateUserCommand(
            String email,
            String defaultCurrency
    ) {}
}
