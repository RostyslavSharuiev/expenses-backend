package rostyslav.sharuiev.expenses.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rostyslav.sharuiev.expenses.auth.entity.RefreshTokenEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    List<RefreshTokenEntity> findAllByTokenFamilyId(UUID tokenFamilyId);
}
