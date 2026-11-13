package rostyslav.sharuiev.expenses.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rostyslav.sharuiev.expenses.user.entity.UserCredentialsEntity;

import java.util.UUID;

public interface UserCredentialsRepository extends JpaRepository<UserCredentialsEntity, UUID> {
}
