package rostyslav.sharuiev.expenses.auth.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rostyslav.sharuiev.expenses.auth.entity.RefreshTokenEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    List<RefreshTokenEntity> findAllByTokenFamilyIdAndRevokedAtIsNull(UUID tokenFamilyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT token
            FROM RefreshTokenEntity token
            WHERE token.tokenHash = :tokenHash
    """)
    Optional<RefreshTokenEntity> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );

    List<RefreshTokenEntity> findAllByUser_IdAndRevokedAtIsNull(UUID userId);
}
