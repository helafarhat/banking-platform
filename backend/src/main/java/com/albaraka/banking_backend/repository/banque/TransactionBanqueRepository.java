package com.albaraka.banking_backend.repository.banque;

import com.albaraka.banking_backend.entity.banque.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface TransactionBanqueRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCompteIdAndDateBetweenOrderByDateAsc(
        Long compteId, LocalDate start, LocalDate end
    );
}