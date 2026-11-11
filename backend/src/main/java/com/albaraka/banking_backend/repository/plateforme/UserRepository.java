package com.albaraka.banking_backend.repository.plateforme;

import com.albaraka.banking_backend.entity.plateforme.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByClientId(Long clientId);
    boolean existsByUsername(String username);
}