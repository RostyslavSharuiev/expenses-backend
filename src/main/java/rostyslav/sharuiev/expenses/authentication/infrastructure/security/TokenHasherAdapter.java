package rostyslav.sharuiev.expenses.authentication.infrastructure.security;

import org.springframework.stereotype.Component;
import rostyslav.sharuiev.expenses.authentication.application.port.out.TokenHasherPort;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class TokenHasherAdapter implements TokenHasherPort {

    private static final String ALGORITHM = "SHA-256";

    @Override
    public String hash(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Token must not be blank");
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM);

            byte[] hash = messageDigest.digest(rawToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw  new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }
}
