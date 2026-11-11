package com.albaraka.banking_backend.entity.banque;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "compte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false, unique = true, length = 20)
    private String rib;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal solde;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_compte", nullable = false)
    private TypeCompte typeCompte;

    @Column(nullable = false)
    private boolean actif = true;

    @OneToMany(mappedBy = "compte", fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    public enum TypeCompte {
        COURANT, EPARGNE
    }
}