package rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.adapter;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rostyslav.sharuiev.expenses.authentication.infrastructure.persistence.entity.EmailVerificationTokenJpaEntity;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataEmailVerificationTokenRepository  extends JpaRepository<EmailVerificationTokenJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT token
            FROM EmailVerificationTokenJpaEntity token
            WHERE token.tokenHash = :tokenHash
            """)
    Optional<EmailVerificationTokenJpaEntity> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query("""
            UPDATE EmailVerificationTokenJpaEntity token
            SET token.invalidatedAt = CURRENT TIMESTAMP
            WHERE token.userId = :userId
            AND token.usedAt IS NULL
            AND token.invalidatedAt IS NULL
            """)
    int invalidateAllByUserId(@Param("userId") UUID userId);
}
