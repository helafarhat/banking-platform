package com.albaraka.banking_backend.repository.banque;

import com.albaraka.banking_backend.entity.banque.Compte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CompteBanqueRepository extends JpaRepository<Compte, Long> {

    @Query("SELECT c FROM Compte c JOIN FETCH c.client WHERE c.rib = :rib")
    Optional<Compte> findByRibWithClient(@Param("rib") String rib);

    List<Compte> findByClientIdAndActifTrue(Long clientId);

    boolean existsByRib(String rib);
}