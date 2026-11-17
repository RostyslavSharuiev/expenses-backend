package rostyslav.sharuiev.expenses.shared.infrastructure.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rostyslav.sharuiev.expenses.session.application.exception.RefreshTokenReuseException;
import rostyslav.sharuiev.expenses.shared.infrastructure.web.response.ApiErrorResponse;

import java.time.Clock;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Clock clock;

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        var response = new ApiErrorResponse(
                clock.instant(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage()
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler(RefreshTokenReuseException.class)
    public ResponseEntity<ApiErrorResponse> handleRefreshTokenReuseException(RefreshTokenReuseException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        var response = new ApiErrorResponse(
                clock.instant(),
                status.value(),
                status.getReasonPhrase(),
                "Invalid or expired refresh token"
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}
