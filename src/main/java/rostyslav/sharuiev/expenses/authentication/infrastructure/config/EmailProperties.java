package rostyslav.sharuiev.expenses.authentication.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email")
public record EmailProperties(
        String from,
        String frontendBaseUrl
) {
    public EmailProperties {
        if (from == null || from.isBlank()) {
            throw new IllegalArgumentException("from must not be blank");
        }

        if (frontendBaseUrl == null || frontendBaseUrl.isBlank()) {
            throw new IllegalArgumentException("frontendBaseUrl must not be blank");
        }
    }
}
