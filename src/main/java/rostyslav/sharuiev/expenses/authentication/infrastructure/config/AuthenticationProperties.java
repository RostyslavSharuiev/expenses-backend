package rostyslav.sharuiev.expenses.authentication.infrastructure.config;

import io.jsonwebtoken.io.Decoders;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.authentication")
public record AuthenticationProperties(
        Duration emailVerificationTokenTtl,
        Duration passwordResetTokenTtl,
        String jwtSecret,
        Duration accessTokenTtl,
        String jwtIssuer
) {
    public AuthenticationProperties {
        validateDuration(emailVerificationTokenTtl, "emailVerificationTokenTtl");
        validateDuration(passwordResetTokenTtl, "passwordResetTokenTtl");
        validateDuration(accessTokenTtl, "accessTokenTtl");

        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalArgumentException("jwtSecret must not be blank");
        }

        byte[] decodedSecret = Decoders.BASE64.decode(jwtSecret);

        if (decodedSecret.length < 32) {
            throw new IllegalArgumentException("jwtSecret must be at least 256 bits");
        }

        if (jwtIssuer == null || jwtIssuer.isBlank()) {
            throw new IllegalArgumentException("jwtIssuer must not be blank");
        }
    }

    private static void validateDuration(Duration duration, String propertyName) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(
                    String.format("%s must be positive", propertyName)
            );
        }
    }
}