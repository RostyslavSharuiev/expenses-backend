package rostyslav.sharuiev.expenses.auth.dto;

public record AuthResult(

        String accessToken,

        String refreshToken,

        long expiresIn
) {
    public AuthResponse toResponse() {
        return new AuthResponse(
                accessToken,
                "Bearer",
                expiresIn / 1000
        );
    }
}
