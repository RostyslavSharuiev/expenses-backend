package rostyslav.sharuiev.expenses.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.auth.dto.AuthResponse;
import rostyslav.sharuiev.expenses.auth.dto.LoginRequest;
import rostyslav.sharuiev.expenses.auth.dto.RegisterRequest;
import rostyslav.sharuiev.expenses.auth.entity.RefreshTokenEntity;
import rostyslav.sharuiev.expenses.auth.exception.EmailAlreadyRegisteredException;
import rostyslav.sharuiev.expenses.auth.exception.InvalidRefreshTokenException;
import rostyslav.sharuiev.expenses.auth.repository.UserCredentialsRepository;
import rostyslav.sharuiev.expenses.auth.security.JwtService;
import rostyslav.sharuiev.expenses.user.dto.UserResponse;
import rostyslav.sharuiev.expenses.user.entity.UserCredentialsEntity;
import rostyslav.sharuiev.expenses.user.entity.UserEntity;
import rostyslav.sharuiev.expenses.user.mapper.UserMapper;
import rostyslav.sharuiev.expenses.user.repository.UserRepository;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserCredentialsRepository credentialsRepository;
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @Value("${app.jwt.expiration}")
    private long accessExpiration;

    @Transactional
    public AuthResult register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException(
                    String.format("User with email %s is already registered", email)
            );
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setEmailVerified(false);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        user = userRepository.save(user);

        UserCredentialsEntity credentials = new UserCredentialsEntity();
        credentials.setUser(user);
        credentials.setPasswordHash(passwordEncoder.encode(request.password()));
        credentials.setPasswordChangedAt(Instant.now());

        credentialsRepository.save(credentials);

        UUID tokenFamilyId = UUID.randomUUID();
        return createAuthResult(user, tokenFamilyId);
    }

    @Transactional
    public AuthResult login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("User with email %s not found", email)
                ));

        UUID tokenFamilyId = UUID.randomUUID();
        return createAuthResult(user, tokenFamilyId);
    }

    @Transactional
    public AuthResult refreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token is required");
        }

        RefreshTokenEntity oldToken = refreshTokenService.rotateRefreshToken(rawRefreshToken);
        UserEntity user = oldToken.getUser();

        String newRefreshToken = refreshTokenService.createRefreshToken(user, oldToken.getTokenFamilyId());
        String newAccessToken = jwtService.generateAccessToken(user.getEmail());

        return new AuthResult(newAccessToken, newRefreshToken, accessExpiration);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenService.revokeToken(rawRefreshToken);
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        String normalized = normalizeEmail(email);
        UserEntity user = userRepository.findByEmailIgnoreCase(normalized)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("User with email %s not found", email)
                ));

        return userMapper.toResponse(user);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private AuthResult createAuthResult(UserEntity user, UUID tokenFamilyId) {
        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user, tokenFamilyId);

        return new AuthResult(accessToken, refreshToken, accessExpiration);
    }

    public record AuthResult(
            String accessToken,
            String refreshToken,
            long expiresIn
    ) {
        public AuthResponse toResponse() {
            return new AuthResponse(
                    accessToken,
                    "Bearer",
                    expiresIn / 1000
            );
        }
    }
}
