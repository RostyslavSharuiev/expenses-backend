package rostyslav.sharuiev.expenses.user.infrastructure.persistence.entity;

import jakarta.persistence.*;
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
@Setter
@NoArgsConstructor()
public class UserJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 255)
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
}
