package rostyslav.sharuiev.expenses.authentication.infrastructure.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rostyslav.sharuiev.expenses.authentication.application.port.in.AuthenticateUseCase;
import rostyslav.sharuiev.expenses.authentication.application.port.in.RegisterAccountUseCase;
import rostyslav.sharuiev.expenses.authentication.infrastructure.web.request.RegisterAccountRequest;
import rostyslav.sharuiev.expenses.authentication.infrastructure.web.response.RegisterAccountResponse;
import rostyslav.sharuiev.expenses.session.infrastructure.config.SessionProperties;
import rostyslav.sharuiev.expenses.authentication.infrastructure.web.request.LoginRequest;
import rostyslav.sharuiev.expenses.authentication.infrastructure.web.response.LoginResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final AuthenticateUseCase authenticateUseCase;
    private final RegisterAccountUseCase registerAccountUseCase;
    private final SessionProperties sessionProperties;

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
}
