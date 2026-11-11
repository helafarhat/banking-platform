package com.albaraka.banking_backend.entity.banque;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "client")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true, length = 8)
    private String cin;

    @Column(unique = true, length = 20)
    private String passport;

    private String tel;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_document", nullable = false)
    private TypeDocument typeDocument = TypeDocument.CIN;

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    private List<Compte> comptes;

    public enum TypeDocument {
        CIN, PASSEPORT
    }
}