package rostyslav.sharuiev.expenses.session.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rostyslav.sharuiev.expenses.session.infrastructure.persistence.entity.RefreshTokenJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT token
            FROM RefreshTokenJpaEntity token
            WHERE token.tokenHash = :tokenHash
            """)
    Optional<RefreshTokenJpaEntity> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    List<RefreshTokenJpaEntity> findAllBySessionId(UUID sessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT token
            FROM RefreshTokenJpaEntity token
            WHERE token.sessionId = :sessionId
            """)
    List<RefreshTokenJpaEntity> findAllBySessionIdForUpdate(@Param("sessionId") UUID sessionId);

    List<RefreshTokenJpaEntity> findAllByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT token
            FROM RefreshTokenJpaEntity token
            WHERE token.userId = :userId
            """)
    List<RefreshTokenJpaEntity> findAllByUserIdForUpdate(@Param("userId") UUID userId);

}
