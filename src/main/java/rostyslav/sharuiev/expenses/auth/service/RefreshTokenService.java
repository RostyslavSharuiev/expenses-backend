package rostyslav.sharuiev.expenses.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.auth.entity.RefreshTokenEntity;
import rostyslav.sharuiev.expenses.auth.exception.InvalidRefreshTokenException;
import rostyslav.sharuiev.expenses.auth.exception.RefreshTokenReuseException;
import rostyslav.sharuiev.expenses.auth.repository.RefreshTokenRepository;
import rostyslav.sharuiev.expenses.user.entity.UserEntity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    public String createRefreshToken(
            UserEntity user,
            UUID tokenFamilyId
    ) {
        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        RefreshTokenEntity entity = new RefreshTokenEntity();

        entity.setUser(user);
        entity.setTokenHash(hashToken(rawToken));
        entity.setTokenFamilyId(tokenFamilyId);
        entity.setExpiresAt(
                Instant.now().plusMillis(refreshExpiration)
        );

        refreshTokenRepository.save(entity);

        return rawToken;
    }

    @Transactional(noRollbackFor = RefreshTokenReuseException.class)
    public RefreshTokenEntity rotateRefreshToken(String rawToken) {

        String tokenHash = hashToken(rawToken);

        RefreshTokenEntity oldToken = refreshTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        Instant now = Instant.now();

        if (oldToken.getRevokedAt() != null) {
            revokeFamily(oldToken.getTokenFamilyId(), now);

            throw new RefreshTokenReuseException("Refresh token reuse detected");
        }

        if (!oldToken.getExpiresAt().isAfter(now)) {
            throw new InvalidRefreshTokenException("Refresh token expired");
        }

        oldToken.setRevokedAt(now);

        refreshTokenRepository.save(oldToken);

        return oldToken;
    }

    @Transactional
    public void revokeFamily(UUID tokenFamilyId) {
        revokeFamily(tokenFamilyId, Instant.now());
    }

    private void revokeFamily(UUID tokenFamilyId, Instant now) {
        var tokens = refreshTokenRepository.findAllByTokenFamilyIdAndRevokedAtIsNull(tokenFamilyId);

        tokens.forEach(token -> token.setRevokedAt(now));

        refreshTokenRepository.saveAll(tokens);
    }

    @Transactional
    public void revokeToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        String tokenHash = hashToken(rawToken);

        refreshTokenRepository.findByTokenHashForUpdate(tokenHash).ifPresent(token -> {
            Instant now = Instant.now();
            token.setRevokedAt(now);
            refreshTokenRepository.save(token);
            revokeFamily(token.getTokenFamilyId(), now);
        });
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        Instant now = Instant.now();
        var tokens = refreshTokenRepository.findAllByUser_IdAndRevokedAtIsNull(userId);

        tokens.forEach(token -> token.setRevokedAt(now));

        refreshTokenRepository.saveAll(tokens);
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable", e
            );
        }
    }
}