package com.albaraka.banking_backend.service;

import com.albaraka.banking_backend.dto.*;
import com.albaraka.banking_backend.entity.banque.Client;
import com.albaraka.banking_backend.entity.banque.Compte;
import com.albaraka.banking_backend.entity.plateforme.OtpCode;
import com.albaraka.banking_backend.entity.plateforme.User;
import com.albaraka.banking_backend.repository.banque.*;
import com.albaraka.banking_backend.repository.plateforme.*;
import com.albaraka.banking_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ClientBanqueRepository clientBanqueRepo;
    private final CompteBanqueRepository compteBanqueRepo;
    private final UserRepository userRepo;
    private final OtpCodeRepository otpCodeRepo;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ── REGISTER ─────────────────────────────────────────────────────────
    public String register(RegisterRequest request) {

        // 1. Check RIB exists in banque_db
        Compte compte = compteBanqueRepo.findByRibWithClient(request.getRib())
            .orElseThrow(() -> new RuntimeException("RIB introuvable dans notre système"));

        // 2. Get the client linked to this compte
        Client client = compte.getClient();

        // 3. Verify document type and number match
        if ("CIN".equals(request.getTypeDocument())) {
            if (client.getCin() == null || !client.getCin().equals(request.getNumeroDocument())) {
                throw new RuntimeException("Numéro CIN incorrect");
            }
        } else if ("PASSEPORT".equals(request.getTypeDocument())) {
            if (client.getPassport() == null || !client.getPassport().equals(request.getNumeroDocument())) {
                throw new RuntimeException("Numéro de passeport incorrect");
            }
        } else {
            throw new RuntimeException("Type de document invalide");
        }

        // 4. Check client doesn't already have a platform account
        if (userRepo.existsByClientId(client.getId())) {
            throw new RuntimeException("Un compte existe déjà pour ce client");
        }

        // 5. Generate username: prenom.firstletter (ex: mohamed.b)
        String username = generateUsername(client.getPrenom(), client.getNom());

        // 6. Generate random password
        String generatedPassword = generatePassword();

        // 7. Create user in plateforme_db
        User user = User.builder()
            .clientId(client.getId())
            .username(username)
            .email(client.getEmail())
            .password(passwordEncoder.encode(generatedPassword))
            .passwordChanged(false)
            .build();

        userRepo.save(user);

        // 8. Send email with credentials
        emailService.sendCredentials(client.getEmail(), client.getPrenom(),
                                     username, generatedPassword);

        return "Compte créé avec succès. Vos identifiants ont été envoyés à " + maskEmail(client.getEmail());
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────
    public Object login(LoginRequest request) {
        User user = userRepo.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Identifiants incorrects"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Identifiants incorrects");
        }

        // If password not changed yet → return flag to force password change
        if (!user.isPasswordChanged()) {
            // Send reminder email
            emailService.sendPasswordReminder(user.getEmail());
            return java.util.Map.of(
                "mustChangePassword", true,
                "username", user.getUsername()
            );
        }

        // Password changed → send OTP
        String otp = generateAndSaveOtp(user.getId());
        emailService.sendOtp(user.getEmail(), otp);

        return java.util.Map.of(
            "mustChangePassword", false,
            "canal", "EMAIL",
            "contact", maskEmail(user.getEmail()),
            "username", user.getUsername()
        );
    }

    // ── CHANGE PASSWORD ───────────────────────────────────────────────────
    public String changePassword(ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }

        User user = userRepo.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChanged(true);
        userRepo.save(user);

        // Send OTP after password change
        String otp = generateAndSaveOtp(user.getId());
        emailService.sendOtp(user.getEmail(), otp);

        return "Mot de passe modifié avec succès";
    }

    // ── VERIFY OTP ────────────────────────────────────────────────────────
    public String verifyOtp(OtpRequest request) {
        User user = userRepo.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        OtpCode otpCode = otpCodeRepo
            .findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(user.getId())
            .orElseThrow(() -> new RuntimeException("Code OTP introuvable"));

        if (otpCode.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Code OTP expiré");

        if (!otpCode.getCode().equals(request.getOtp()))
            throw new RuntimeException("Code OTP incorrect");

        otpCode.setUsed(true);
        otpCodeRepo.save(otpCode);

        return jwtUtil.generateToken(user.getUsername());
    }

    // ── RESEND OTP ────────────────────────────────────────────────────────
    public void resendOtp(String username) {
        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        String otp = generateAndSaveOtp(user.getId());
        emailService.sendOtp(user.getEmail(), otp);
    }

    // ── ME (dashboard info) ───────────────────────────────────────────────
    public java.util.Map<String, Object> getMe(String username) {
        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        Client client = clientBanqueRepo.findById(user.getClientId())
            .orElseThrow(() -> new RuntimeException("Client introuvable"));

        return java.util.Map.of(
            "id",        user.getId(),
            "clientId",  client.getId(),
            "nom",       client.getNom(),
            "prenom",    client.getPrenom(),
            "email",     user.getEmail(),
            "username",  user.getUsername()
        );
    }

    // ── HELPERS ───────────────────────────────────────────────────────────
    private String generateAndSaveOtp(Long userId) {
        String code = String.format("%06d", new Random().nextInt(999999));
        OtpCode otp = OtpCode.builder()
            .userId(userId)
            .code(code)
            .expiresAt(LocalDateTime.now().plusMinutes(5))
            .used(false)
            .build();
        otpCodeRepo.save(otp);
        return code;
    }

    private String generateUsername(String prenom, String nom) {
        String base = prenom.toLowerCase().replaceAll("\\s+", "") + "."
                    + nom.toLowerCase().charAt(0);
        String username = base;
        int counter = 1;
        while (userRepo.existsByUsername(username)) {
            username = base + counter++;
        }
        return username;
    }

    private String generatePassword() {
        // 8 chars: letters + numbers
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        return email.charAt(0) + "***" + email.substring(at);
    }
}