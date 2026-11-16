package rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.entity.UserCredentialsJpaEntity;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserCredentialsRepository extends JpaRepository<UserCredentialsJpaEntity, UUID> {

    Optional<UserCredentialsJpaEntity> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
