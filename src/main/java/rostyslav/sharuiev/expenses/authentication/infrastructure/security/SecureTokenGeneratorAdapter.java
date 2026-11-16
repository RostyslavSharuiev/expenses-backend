package rostyslav.sharuiev.expenses.authentication.infrastructure.security;

import org.springframework.stereotype.Component;
import rostyslav.sharuiev.expenses.authentication.application.port.out.SecureTokenGeneratorPort;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SecureTokenGeneratorAdapter implements SecureTokenGeneratorPort {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    @Override
    public String generateToken() {
        byte[] randomBytes = new byte[TOKEN_BYTES];

        secureRandom.nextBytes(randomBytes);

        return encoder.encodeToString(randomBytes);
    }
}
