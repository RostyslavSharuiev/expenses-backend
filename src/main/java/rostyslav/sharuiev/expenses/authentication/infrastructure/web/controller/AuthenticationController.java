package rostyslav.sharuiev.expenses.authentication.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rostyslav.sharuiev.expenses.authentication.application.port.in.*;
import rostyslav.sharuiev.expenses.authentication.infrastructure.web.request.*;
import rostyslav.sharuiev.expenses.authentication.infrastructure.web.response.RegisterAccountResponse;
import rostyslav.sharuiev.expenses.authentication.infrastructure.web.response.ResetPasswordResponse;
import rostyslav.sharuiev.expenses.session.application.port.in.SessionManagementUseCase;
import rostyslav.sharuiev.expenses.session.infrastructure.config.SessionProperties;
import rostyslav.sharuiev.expenses.authentication.infrastructure.web.response.LoginResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "Endpoints for account registration, authentication, email verification and session management"
)
public class AuthenticationController {

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final AuthenticateUseCase authenticateUseCase;
    private final RegisterAccountUseCase registerAccountUseCase;
    private final SessionProperties sessionProperties;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResendVerificationEmailUseCase resendVerificationEmailUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final RefreshAuthenticationUseCase refreshAuthenticationUseCase;
    private final SessionManagementUseCase sessionManagementUseCase;

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = """
                    Authenticates a user using email and password.
                    
                    Returns an access token in the response body and sets a refresh token in an HttpOnly cookie.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Authentication successful"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            )
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = authenticateUseCase.authenticate(
                new AuthenticateUseCase.AuthenticateCommand(
                        request.email(),
                        request.password()
                )
        );

        var refreshTokenCookie = ResponseCookie
                .from(REFRESH_TOKEN_COOKIE, result.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(sessionProperties.refreshTokenTtl())
                .build();

        var response = new LoginResponse(
                result.userId(),
                result.accessToken(),
                result.accessTokenExpiresIn()
        );

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register account",
            description = """
                    Creates a new user account and initiates email verification.
                    
                    An email verification message is sent to the provided email address.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Account successfully created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            )
    })
    public ResponseEntity<RegisterAccountResponse> register(@Valid @RequestBody RegisterAccountRequest request) {
        var result = registerAccountUseCase.register(
                new RegisterAccountUseCase.RegisterAccountCommand(
                        request.email(),
                        request.password(),
                        request.defaultCurrency()
                )
        );

        var response = new RegisterAccountResponse(
                result.userId(),
                result.email()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        verifyEmailUseCase.verifyEmail(request.token());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<Void> resendVerificationEmail(@Valid @RequestBody ResendVerificationEmailRequest request) {
        resendVerificationEmailUseCase.resendVerificationEmail(request.email());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        var result = resetPasswordUseCase.resetPassword(
                new ResetPasswordUseCase.ResetPasswordCommand(
                        request.token(),
                        request.newPassword()
                )
        );

        var refreshTokenCookie = ResponseCookie
                .from(REFRESH_TOKEN_COOKIE, result.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(sessionProperties.refreshTokenTtl())
                .build();

        var response = new ResetPasswordResponse(
                result.accessToken(),
                result.accessTokenExpiresIn()
        );

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        forgotPasswordUseCase.forgotPassword(request.email());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh authentication tokens",
            description = """
                    Rotates the refresh token and issues a new access token.
                    
                    The refresh token must be provided in the refresh_token cookie.
                    The previous refresh token becomes invalid.
                    
                    The new refresh token is returned as an HttpOnly cookie.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens successfully refreshed"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token is missing, invalid, expired or revoked"
            )
    })
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken
    ) {
        var result = refreshAuthenticationUseCase.refresh(refreshToken);

        var cookie = ResponseCookie
                .from(REFRESH_TOKEN_COOKIE, result.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(sessionProperties.refreshTokenTtl())
                .build();

        var response = new LoginResponse(
                result.userId(),
                result.accessToken(),
                result.accessTokenExpiresIn()
        );

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout",
            description = """
                    Revokes the current session associated with the refresh token and clears the refresh_token cookie.
                    
                    The endpoint is idempotent. It returns success even if the refresh token is missing or invalid.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Session revoked and refresh cookie cleared"
            )
    })
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken
    ) {
        sessionManagementUseCase.revokeSession(refreshToken);

        var cookie = ResponseCookie
                .from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(0)
                .build();

        return ResponseEntity
                .noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
