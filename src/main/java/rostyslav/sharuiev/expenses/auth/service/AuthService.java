package rostyslav.sharuiev.expenses.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.auth.dto.AuthResult;
import rostyslav.sharuiev.expenses.auth.dto.LoginRequest;
import rostyslav.sharuiev.expenses.auth.dto.RegisterRequest;
import rostyslav.sharuiev.expenses.auth.exception.EmailAlreadyRegisteredException;
import rostyslav.sharuiev.expenses.auth.mapper.AuthMapper;
import rostyslav.sharuiev.expenses.auth.repository.UserCredentialsRepository;
import rostyslav.sharuiev.expenses.auth.security.JwtService;
import rostyslav.sharuiev.expenses.user.dto.UserResponse;
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
    private final AuthMapper authMapper;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @Transactional
    public AuthResult register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException(
                    String.format("User with email %s is already registered", email)
            );
        }

        var user = userRepository.save(authMapper.toUserEntity(request, email, false));

        credentialsRepository.save(authMapper.toUserCredentialsEntity(
                user,
                passwordEncoder.encode(request.password()),
                Instant.now()
        ));

        return createAuthResult(user);
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

        return createAuthResult(user);
    }

    @Transactional
    public AuthResult refreshToken(String rawRefreshToken) {
        RefreshTokenService.RefreshTokenResult refreshResult = refreshTokenService.rotateRefreshToken(rawRefreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(refreshResult.email());

        String accessToken = jwtService.generateAccessToken(userDetails);

        return new AuthResult(accessToken, refreshResult.refreshToken(), jwtService.getExpiration());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeToken(rawRefreshToken);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        String normalizedEmail = normalizeEmail(email);

        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("User with email %s not found", email)
                ));

        return userMapper.toResponse(user);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private AuthResult createAuthResult(UserEntity user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(user, UUID.randomUUID());

        return new AuthResult(accessToken, refreshToken, jwtService.getExpiration());
    }
}
