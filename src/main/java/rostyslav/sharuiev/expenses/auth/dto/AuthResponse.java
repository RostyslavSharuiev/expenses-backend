package rostyslav.sharuiev.expenses.auth.dto;

public record AuthResponse(

        String accessToken,

        String tokenType,

        long expiresIn
) {
}
