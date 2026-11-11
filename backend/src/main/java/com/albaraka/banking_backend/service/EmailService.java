package com.albaraka.banking_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Send generated credentials after registration
    public void sendCredentials(String to, String prenom, String username, String password) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Vos identifiants — Al Baraka Bank");
        msg.setText(
            "Bonjour " + prenom + ",\n\n" +
            "Votre compte Al Baraka Bank a été créé avec succès.\n\n" +
            "Vos identifiants de connexion :\n" +
            "Nom d'utilisateur : " + username + "\n" +
            "Mot de passe temporaire : " + password + "\n\n" +
            "⚠️ Vous devez changer votre mot de passe lors de votre première connexion.\n\n" +
            "Al Baraka Bank"
        );
        mailSender.send(msg);
    }

    // Send OTP code
    public void sendOtp(String to, String otp) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Code de vérification — Al Baraka Bank");
        msg.setText(
            "Bonjour,\n\n" +
            "Votre code de vérification est : " + otp + "\n\n" +
            "Ce code expire dans 5 minutes.\n\n" +
            "Al Baraka Bank"
        );
        mailSender.send(msg);
    }

    // Send password change reminder
    public void sendPasswordReminder(String to) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Action requise — Changez votre mot de passe");
        msg.setText(
            "Bonjour,\n\n" +
            "Vous devez changer votre mot de passe temporaire avant d'accéder à la plateforme.\n\n" +
            "Connectez-vous et suivez les instructions pour définir un nouveau mot de passe.\n\n" +
            "Al Baraka Bank"
        );
        mailSender.send(msg);
    }
}