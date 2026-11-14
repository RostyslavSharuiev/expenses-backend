package rostyslav.sharuiev.expenses.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expiration;

    public JwtService(
       @Value("${app.jwt.secret}") String secret,
       @Value("${app.jwt.expiration}") long expiration
    ) {
        this.signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );

        this.expiration = expiration;
    }

    public String generateAccessToken(UserDetails userDetails) {
        return generateAccessToken(userDetails.getUsername());
    }

    public String generateAccessToken(String username) {
        Instant now = Instant.now();

        return Jwts
                .builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expiration)))
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = extractAllClaims(token);

            String username = claims.getSubject();
            Date expirationDate = claims.getExpiration();

            return username != null
                    && username.equals(userDetails.getUsername())
                    && expirationDate != null
                    && expirationDate.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpiration() {
        return expiration;
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
