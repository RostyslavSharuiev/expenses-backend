package rostyslav.sharuiev.expenses.authentication.infrastructure.web.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank
        String token
) {
}
