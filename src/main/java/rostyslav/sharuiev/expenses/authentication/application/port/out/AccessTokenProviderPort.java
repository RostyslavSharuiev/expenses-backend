package rostyslav.sharuiev.expenses.authentication.application.port.out;

import java.util.UUID;

public interface AccessTokenProviderPort {

    AccessToken generate(UUID userId);

    record AccessToken(
            String value,
            long expiresIn
    ) {}
}