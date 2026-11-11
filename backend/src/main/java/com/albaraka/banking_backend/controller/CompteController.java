package com.albaraka.banking_backend.controller;

import com.albaraka.banking_backend.service.CompteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comptes")
@RequiredArgsConstructor
public class CompteController {

    private final CompteService compteService;

    @GetMapping("/mes-comptes")
    public ResponseEntity<?> getMesComptes(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(compteService.getMesComptes(userDetails.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/releve")
    public ResponseEntity<?> getReleve(
            @PathVariable Long id,
            @RequestParam String mois,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(compteService.getReleve(id, mois, userDetails.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}