package com.albaraka.banking_backend.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class RegisterRequest {
    // RIB must be 20 digits starting with 32
    @NotBlank
    @Pattern(regexp = "32\\d{18}", message = "RIB invalide")
    private String rib;

    // CIN or PASSEPORT
    @NotBlank
    private String typeDocument;

    // The actual document number (8 digits for CIN, or passport number)
    @NotBlank
    private String numeroDocument;
}