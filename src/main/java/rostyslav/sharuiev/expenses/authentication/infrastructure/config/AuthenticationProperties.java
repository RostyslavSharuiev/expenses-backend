package rostyslav.sharuiev.expenses.authentication.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.authentication")
public record AuthenticationProperties(
        Duration emailVerificationTokenTtl
) {
}
