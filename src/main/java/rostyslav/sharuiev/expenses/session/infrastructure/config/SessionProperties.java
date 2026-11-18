package rostyslav.sharuiev.expenses.session.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.session")
public record SessionProperties(
        Duration refreshTokenTtl
) {
}
