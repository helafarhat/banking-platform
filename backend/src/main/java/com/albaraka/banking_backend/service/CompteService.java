package com.albaraka.banking_backend.service;

import com.albaraka.banking_backend.entity.banque.Compte;
import com.albaraka.banking_backend.entity.banque.Transaction;
import com.albaraka.banking_backend.entity.plateforme.User;
import com.albaraka.banking_backend.repository.banque.*;
import com.albaraka.banking_backend.repository.plateforme.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompteService {

    private final CompteBanqueRepository compteBanqueRepo;
    private final TransactionBanqueRepository transactionBanqueRepo;
    private final UserRepository userRepo;

    public List<Map<String, Object>> getMesComptes(String username) {
        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return compteBanqueRepo.findByClientIdAndActifTrue(user.getClientId())
            .stream()
            .map(c -> Map.<String, Object>of(
                "id",         c.getId(),
                "rib",        c.getRib(),
                "ribFormate", formatRib(c.getRib()),
                "solde",      c.getSolde(),
                "typeCompte", c.getTypeCompte().name(),
                "actif",      c.isActif()
            ))
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getReleve(Long compteId, String mois, String username) {
        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Compte compte = compteBanqueRepo.findById(compteId)
            .orElseThrow(() -> new RuntimeException("Compte introuvable"));

        // Security check: compte must belong to this user's client
        if (!compte.getClient().getId().equals(user.getClientId())) {
            throw new RuntimeException("Accès refusé");
        }

        YearMonth ym = YearMonth.parse(mois);
        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();

        return transactionBanqueRepo
            .findByCompteIdAndDateBetweenOrderByDateAsc(compteId, start, end)
            .stream()
            .map(t -> Map.<String, Object>of(
                "date",       t.getDate().toString(),
                "libelle",    t.getLibelle(),
                "type",       t.getType().name(),
                "montant",    t.getMontant(),
                "soldeApres", t.getSoldeApres()
            ))
            .collect(Collectors.toList());
    }

    private String formatRib(String rib) {
        if (rib == null || rib.length() != 20) return rib;
        return rib.replaceAll("(.{2})(.{4})(.{4})(.{4})(.{4})(.{2})", "$1 $2 $3 $4 $5 $6");
    }
}