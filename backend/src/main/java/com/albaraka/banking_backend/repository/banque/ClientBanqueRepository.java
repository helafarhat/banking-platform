package com.albaraka.banking_backend.repository.banque;

import com.albaraka.banking_backend.entity.banque.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClientBanqueRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByCin(String cin);
    Optional<Client> findByPassport(String passport);
}