package com.example.learning_tracker_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "accounts")
@Getter @Setter @NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean mustChangePassword;

    private Instant tempPasswordExpiresAt;
    @Column(nullable = false, columnDefinition = "integer default 0")
    private int sessionVersion;

    public boolean isTemporaryPasswordExpired() {
        return mustChangePassword
                && tempPasswordExpiresAt != null
                && Instant.now().isAfter(tempPasswordExpiresAt);
    }

    @PrePersist
    @PreUpdate
    void normalizeEmail() {
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }
}