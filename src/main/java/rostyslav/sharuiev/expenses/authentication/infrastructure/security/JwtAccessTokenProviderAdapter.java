package rostyslav.sharuiev.expenses.authentication.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import rostyslav.sharuiev.expenses.authentication.application.port.out.AccessTokenProviderPort;
import rostyslav.sharuiev.expenses.authentication.infrastructure.config.AuthenticationProperties;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Component
public class JwtAccessTokenProviderAdapter implements AccessTokenProviderPort {

    private final SecretKey signingKey;
    private final AuthenticationProperties authenticationProperties;
    private final Clock clock;

    public JwtAccessTokenProviderAdapter(
            AuthenticationProperties authenticationProperties,
            Clock clock
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(authenticationProperties.jwtSecret());

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.authenticationProperties = authenticationProperties;
        this.clock = clock;
    }

    @Override
    public AccessToken generate(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        
        Instant now = clock.instant();
        Instant expiresAt = now.plus(authenticationProperties.accessTokenTtl());

        String token = Jwts
                .builder()
                .subject(userId.toString())
                .issuer(authenticationProperties.jwtIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        return new AccessToken(token, authenticationProperties.accessTokenTtl().toSeconds());
    }
}
