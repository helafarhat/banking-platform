package com.albaraka.banking_backend.dto;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String username;
    private String newPassword;
    private String confirmPassword;
}