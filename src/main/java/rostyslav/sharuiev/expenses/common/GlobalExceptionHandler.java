package rostyslav.sharuiev.expenses.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rostyslav.sharuiev.expenses.auth.exception.EmailAlreadyRegisteredException;
import rostyslav.sharuiev.expenses.auth.exception.InvalidRefreshTokenException;
import rostyslav.sharuiev.expenses.auth.exception.RefreshTokenReuseException;
import rostyslav.sharuiev.expenses.common.dto.ErrorResponseDto;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(EmailAlreadyRegisteredException.class)
        public ResponseEntity<ErrorResponseDto> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException e) {
                log.warn("Email already registered: {}", e.getMessage());

                var errorDto = new ErrorResponseDto(
                        "Email already registered",
                        e.getMessage(),
                        LocalDateTime.now());

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(errorDto);
        }

        @ExceptionHandler({ BadCredentialsException.class, AuthenticationException.class })
        public ResponseEntity<ErrorResponseDto> handleAuthenticationException(Exception e) {
                log.warn("Authentication failed: {}", e.getMessage());

                var errorDto = new ErrorResponseDto(
                        "Authentication failed",
                        "Invalid email or password",
                        LocalDateTime.now());

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(errorDto);
        }

        @ExceptionHandler(InvalidRefreshTokenException.class)
        public ResponseEntity<ErrorResponseDto> handleInvalidRefreshToken(InvalidRefreshTokenException e) {
                log.warn("Invalid refresh token: {}", e.getMessage());

                var errorDto = new ErrorResponseDto(
                        "Invalid refresh token",
                        e.getMessage(),
                        LocalDateTime.now());

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(errorDto);
        }

        @ExceptionHandler(RefreshTokenReuseException.class)
        public ResponseEntity<ErrorResponseDto> handleRefreshTokenReuse(RefreshTokenReuseException e) {
                log.warn("Refresh token reuse detected: {}", e.getMessage());

                var errorDto = new ErrorResponseDto(
                        "Refresh token reuse detected",
                        e.getMessage(),
                        LocalDateTime.now());

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(errorDto);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException e) {
                log.warn("Validation error: {}", e.getMessage());

                String detailedMessage = e
                        .getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(err -> err.getField() + ": " + err.getDefaultMessage())
                        .collect(Collectors.joining(", "));

                var errorDto = new ErrorResponseDto(
                        "Validation failed",
                        detailedMessage,
                        LocalDateTime.now());

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(errorDto);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException e) {
                log.warn("Bad request: {}", e.getMessage());

                var errorDto = new ErrorResponseDto(
                        "Bad request",
                        e.getMessage(),
                        LocalDateTime.now());

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(errorDto);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponseDto> handleGenericException(Exception e) {
                log.error("Internal server error", e);

                var errorDto = new ErrorResponseDto(
                        "Internal server error",
                        e.getMessage(),
                        LocalDateTime.now());

                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(errorDto);
        }
}
