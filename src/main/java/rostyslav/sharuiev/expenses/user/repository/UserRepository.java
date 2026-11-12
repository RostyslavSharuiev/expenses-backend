package rostyslav.sharuiev.expenses.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rostyslav.sharuiev.expenses.user.entity.UserEntity;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
}
