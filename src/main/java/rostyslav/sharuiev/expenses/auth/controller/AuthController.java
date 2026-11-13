package rostyslav.sharuiev.expenses.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rostyslav.sharuiev.expenses.auth.dto.AuthResponse;
import rostyslav.sharuiev.expenses.auth.dto.LoginRequest;
import rostyslav.sharuiev.expenses.auth.dto.RefreshTokenRequest;
import rostyslav.sharuiev.expenses.auth.dto.RegisterRequest;
import rostyslav.sharuiev.expenses.auth.service.AuthService;
import rostyslav.sharuiev.expenses.user.dto.UserResponse;

import java.time.Duration;

@Tag(name = "Authentication", description = "Endpoints for user authentication and session management")
@RestController
@RequestMapping({"/api/v1/auth", "/auth"})
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final AuthService authService;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${app.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        log.info("Registration attempt for email={}", request.email());

        AuthService.AuthResult result = authService.register(request);
        ResponseCookie cookie = createRefreshTokenCookie(result.refreshToken(), Duration.ofMillis(refreshExpiration));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result.toResponse());
    }

    @Operation(summary = "Authenticate user with email and password")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        log.info("Login attempt for email={}", request.email());

        AuthService.AuthResult result = authService.login(request);
        ResponseCookie cookie = createRefreshTokenCookie(result.refreshToken(), Duration.ofMillis(refreshExpiration));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result.toResponse());
    }

    @Operation(summary = "Rotate refresh token and get a new access token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            HttpServletRequest httpRequest,
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String cookieToken,
            @RequestBody(required = false) RefreshTokenRequest bodyRequest
    ) {
        String token = resolveRefreshToken(httpRequest, cookieToken, bodyRequest);
        AuthService.AuthResult result = authService.refreshToken(token);
        ResponseCookie cookie = createRefreshTokenCookie(result.refreshToken(), Duration.ofMillis(refreshExpiration));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result.toResponse());
    }

    @Operation(summary = "Logout user and revoke refresh token session")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest httpRequest,
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String cookieToken,
            @RequestBody(required = false) RefreshTokenRequest bodyRequest
    ) {
        String token = resolveRefreshToken(httpRequest, cookieToken, bodyRequest);
        authService.logout(token);
        ResponseCookie clearCookie = createRefreshTokenCookie("", Duration.ZERO);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .build();
    }

    @Operation(summary = "Get currently authenticated user info")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserResponse response = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(response);
    }

    private ResponseCookie createRefreshTokenCookie(String value, Duration maxAge) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    private String resolveRefreshToken(
            HttpServletRequest httpRequest,
            String cookieToken,
            RefreshTokenRequest bodyRequest
    ) {
        if (cookieToken != null && !cookieToken.isBlank()) {
            return cookieToken;
        }
        if (bodyRequest != null && bodyRequest.refreshToken() != null && !bodyRequest.refreshToken().isBlank()) {
            return bodyRequest.refreshToken();
        }
        if (httpRequest.getCookies() != null) {
            for (Cookie c : httpRequest.getCookies()) {
                if ("refresh_token".equalsIgnoreCase(c.getName()) || REFRESH_TOKEN_COOKIE_NAME.equalsIgnoreCase(c.getName())) {
                    if (c.getValue() != null && !c.getValue().isBlank()) {
                        return c.getValue();
                    }
                }
            }
        }
        return null;
    }
}
