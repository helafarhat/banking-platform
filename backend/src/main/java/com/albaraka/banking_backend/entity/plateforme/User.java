package com.albaraka.banking_backend.entity.plateforme;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Links to banque_db.client.id (no FK since different DB)
    @Column(name = "client_id", nullable = false, unique = true)
    private Long clientId;

    // Generated: prenom.firstletter ex: mohamed.b
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // false = must change password, true = already changed
    @Column(name = "password_changed", nullable = false)
    private boolean passwordChanged = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}