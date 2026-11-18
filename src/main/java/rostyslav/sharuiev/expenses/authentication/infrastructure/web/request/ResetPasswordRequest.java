package rostyslav.sharuiev.expenses.authentication.infrastructure.web.request;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank
        String token,

        @NotBlank
        String newPassword
) {
}
