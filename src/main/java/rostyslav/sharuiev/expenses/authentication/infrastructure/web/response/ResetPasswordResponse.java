package rostyslav.sharuiev.expenses.authentication.infrastructure.web.response;

public record ResetPasswordResponse(
        String accessToken,
        long accessTokenExpiresIn
) {
}
