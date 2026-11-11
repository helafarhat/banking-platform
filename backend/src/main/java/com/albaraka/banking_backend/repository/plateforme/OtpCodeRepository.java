package com.albaraka.banking_backend.repository.plateforme;

import com.albaraka.banking_backend.entity.plateforme.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
    Optional<OtpCode> findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(Long userId);
}