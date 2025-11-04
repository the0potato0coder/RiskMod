package com.jugantar.RiskMod.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer"; // Standard practice

    public AuthResponse(String token) {
        this.token = token;
    }
}
