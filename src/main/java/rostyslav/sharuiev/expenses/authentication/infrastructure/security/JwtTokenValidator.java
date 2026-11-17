package rostyslav.sharuiev.expenses.authentication.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import rostyslav.sharuiev.expenses.authentication.infrastructure.config.AuthenticationProperties;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtTokenValidator {

    private final SecretKey signingKey;
    private final String jwtIssuer;
    private final Clock clock;

    public JwtTokenValidator(
            AuthenticationProperties authenticationProperties,
            Clock clock
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(authenticationProperties.jwtSecret());

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.jwtIssuer = authenticationProperties.jwtIssuer();
        this.clock = clock;
    }

    public Optional<UUID> validateAndGetUserId(String token) {
        try {
            Claims claims = Jwts
                    .parser()
                    .verifyWith(signingKey)
                    .requireIssuer(jwtIssuer)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Optional.of(UUID.fromString(claims.getSubject()));
        }
        catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
