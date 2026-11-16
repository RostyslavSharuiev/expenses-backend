package rostyslav.sharuiev.expenses.user.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class UserJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Column
    private Instant emailVerifiedAt;

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @Column(columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(nullable = false, length = 3)
    private String defaultCurrency;

    @Column
    private Instant deletedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public UserJpaEntity(
            UUID id,
            String email,
            Instant emailVerifiedAt,
            String firstName,
            String lastName,
            String avatarUrl,
            String defaultCurrency,
            Instant deletedAt
    ) {
        this.id = id;
        this.email = email;
        this.emailVerifiedAt = emailVerifiedAt;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatarUrl = avatarUrl;
        this.defaultCurrency = defaultCurrency;
        this.deletedAt = deletedAt;
    }

    public void updateFrom(UserJpaEntity source) {
        this.email = source.email;
        this.emailVerifiedAt = source.getEmailVerifiedAt();
        this.firstName = source.firstName;
        this.lastName = source.lastName;
        this.avatarUrl = source.avatarUrl;
        this.defaultCurrency = source.defaultCurrency;
        this.deletedAt = source.deletedAt;
    }
}
