package rostyslav.sharuiev.expenses.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rostyslav.sharuiev.expenses.user.entity.UserCredentialsEntity;

import java.util.Optional;
import java.util.UUID;

public interface UserCredentialsRepository extends JpaRepository<UserCredentialsEntity, UUID> {
    Optional<UserCredentialsEntity> findByUser_EmailIgnoreCase(String email);
}
