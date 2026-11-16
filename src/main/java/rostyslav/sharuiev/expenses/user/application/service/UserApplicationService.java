package rostyslav.sharuiev.expenses.user.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.user.application.port.in.UserAccountPort;
import rostyslav.sharuiev.expenses.user.application.port.out.UserRepositoryPort;
import rostyslav.sharuiev.expenses.user.domain.model.User;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserApplicationService implements UserAccountPort {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public UserAccount create(CreateUserCommand command) {
        if (userRepositoryPort.existsByEmail(command.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.create(
                command.email(),
                command.defaultCurrency()
        );

        User savedUser = userRepositoryPort.save(user);

        return toAccount(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccount> findById(UUID userId) {
        return userRepositoryPort
                .findById(userId)
                .map(this::toAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccount> findByEmail(String email) {
        return userRepositoryPort
                .findByEmail(email)
                .map(this::toAccount);
    }

    @Override
    public void verifyEmail(UUID userId, Instant verifiedAt) {
        User user = userRepositoryPort
                .findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("User not found: %s", userId)
                ));

        user.verifyEmail(verifiedAt);

        userRepositoryPort.save(user);
    }

    private UserAccount toAccount(User user) {
        return new UserAccount(
                user.getId(),
                user.getEmail(),
                user.getEmailVerifiedAt(),
                user.isDeleted()
        );
    }
}
