package com.albaraka.banking_backend.dto;

import lombok.Data;

@Data
public class OtpRequest {
    private String otp;
    private String username;
}