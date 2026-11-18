package rostyslav.sharuiev.expenses.authentication.domain.model;

import lombok.Getter;
import rostyslav.sharuiev.expenses.authentication.domain.enums.OAuthProvider;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Getter
public class OAuthAccount {

    private final UUID id;
    private final UUID userId;
    private final OAuthProvider provider;
    private final String providerUserId;
    private final Instant linkedAt;

    private String providerEmail;
    private Boolean providerEmailVerified;

    private OAuthAccount(
            UUID id,
            UUID userId,
            OAuthProvider provider,
            String providerUserId,
            String providerEmail,
            Boolean providerEmailVerified,
            Instant linkedAt
    ) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.provider = Objects.requireNonNull(provider, "provider cannot be null");
        this.providerUserId = validateProviderUserId(providerUserId);
        this.providerEmail = normalizeEmail(providerEmail);
        this.providerEmailVerified = providerEmailVerified;
        this.linkedAt = Objects.requireNonNull(linkedAt, "linkedAt cannot be null");
    }

    public static OAuthAccount create(
            UUID userId,
            OAuthProvider provider,
            String providerUserId,
            String providerEmail,
            Boolean providerEmailVerified
    ) {
        return new OAuthAccount(
                UUID.randomUUID(),
                userId,
                provider,
                providerUserId,
                providerEmail,
                providerEmailVerified,
                Instant.now()
        );
    }

    public static OAuthAccount reconstitute(
            UUID id,
            UUID userId,
            OAuthProvider provider,
            String providerUserId,
            String providerEmail,
            Boolean providerEmailVerified,
            Instant linkedAt
    ) {
        return new OAuthAccount(
                id,
                userId,
                provider,
                providerUserId,
                providerEmail,
                providerEmailVerified,
                linkedAt
        );
    }

    public void updateProviderProfile(
            String providerEmail,
            Boolean providerEmailVerified
    ) {
        if (providerEmail != null && !providerEmail.isBlank()) {
            this.providerEmail = normalizeEmail(providerEmail);
        }

        if (providerEmailVerified != null) {
            this.providerEmailVerified = providerEmailVerified;
        }
    }

    private static String validateProviderUserId(String providerUserId) {
        Objects.requireNonNull(providerUserId, "providerUserId cannot be null");

        if (providerUserId.isBlank()) {
            throw new IllegalArgumentException("providerUserId cannot be blank");
        }

        return providerUserId;
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}
