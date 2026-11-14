package rostyslav.sharuiev.expenses.user.infrastructure.persistence.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import rostyslav.sharuiev.expenses.user.infrastructure.persistence.entity.UserJpaEntity;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

}
