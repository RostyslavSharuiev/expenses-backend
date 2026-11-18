package rostyslav.sharuiev.expenses.user.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Getter
public class User {

    private final UUID id;

    private String email;
    private Instant emailVerifiedAt;

    private String firstName;
    private String lastName;
    private String avatarUrl;

    private String defaultCurrency;

    private Instant deletedAt;

    private User(
            UUID id,
            String email,
            Instant emailVerifiedAt,
            String firstName,
            String lastName,
            String avatarUrl,
            String defaultCurrency,
            Instant deletedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.email = normalizeEmail(email);
        this.emailVerifiedAt = emailVerifiedAt;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatarUrl = avatarUrl;
        this.defaultCurrency = defaultCurrency;
        this.deletedAt = deletedAt;
    }

    public static User create(
            String email,
            String defaultCurrency
    ) {
        return new User(
                UUID.randomUUID(),
                email,
                null,
                null,
                null,
                null,
                defaultCurrency,
                null
        );
    }

    /**
     * Restoring an existing user from storage.
     * Not use for registration.
     */
    public static User reconstitute(
            UUID id,
            String email,
            Instant emailVerifiedAt,
            String firstName,
            String lastName,
            String avatarUrl,
            String defaultCurrency,
            Instant deletedAt
    ) {
        return new User(
                id,
                email,
                emailVerifiedAt,
                firstName,
                lastName,
                avatarUrl,
                defaultCurrency,
                deletedAt
        );
    }

    public void updateProfile(
            String firstName,
            String lastName,
            String avatarUrl
    ) {
        ensureNotDeleted();

        this.firstName = firstName;
        this.lastName = lastName;
        this.avatarUrl = avatarUrl;
    }

    public void verifyEmail(Instant verifiedAt) {
        ensureNotDeleted();

        if (isEmailVerified()) {
            return;
        }

        this.emailVerifiedAt = Objects.requireNonNull(verifiedAt);
    }

    /**
     * Invoke only after verifying confirmation of the new address
     * at the Application Layer.
     */
    public void changeEmailAfterVerification(
            String newEmail,
            Instant verifiedAt
    ) {
        ensureNotDeleted();

        this.email = normalizeEmail(newEmail);
        this.emailVerifiedAt = Objects.requireNonNull(verifiedAt);
    }

    public void changeDefaultCurrency(String currency) {
        ensureNotDeleted();

        this.defaultCurrency = normalizeCurrency(currency);
    }

    public void delete(Instant deletedAt) {
        if (isDeleted()) {
            return;
        }

        this.deletedAt = Objects.requireNonNull(deletedAt);
    }

    public void restore() {
        if (!isDeleted()) {
            return;
        }

        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isEmailVerified() {
        return emailVerifiedAt != null;
    }

    private void ensureNotDeleted() {
        if (isDeleted()) {
            throw new IllegalStateException("Deleted user cannot be modified");
        }
    }

    private static String normalizeEmail(String email) {
        Objects.requireNonNull(email, "Email must not be null");

        String normalized = email.trim().toLowerCase(Locale.ROOT);

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }

        return normalized;
    }

    private static String normalizeCurrency(String currency) {
        Objects.requireNonNull(currency, "Currency must not be null");

        String normalized = currency.trim().toUpperCase(Locale.ROOT);

        if (!normalized.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Currency must be exactly 3 Latin letters");
        }

        return normalized;
    }
}
