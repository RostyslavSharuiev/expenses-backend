package rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataPasswordResetTokenRepository extends JpaRepository<PasswordResetTokenJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT token
            FROM PasswordResetTokenJpaEntity token
            WHERE token.tokenHash = :tokenHash
            """)
    Optional<PasswordResetTokenJpaEntity> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query("""
            UPDATE PasswordResetTokenJpaEntity token
            SET token.invalidatedAt = CURRENT_TIMESTAMP
            WHERE token.userId = :userId
            AND token.usedAt IS NULL
            AND token.invalidatedAt IS NULL
            """)
    int invalidateAllByUserId(@Param("userId") UUID userId);
}
