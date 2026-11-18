package rostyslav.sharuiev.expenses.authentication.infrastructure.web.controller;

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
