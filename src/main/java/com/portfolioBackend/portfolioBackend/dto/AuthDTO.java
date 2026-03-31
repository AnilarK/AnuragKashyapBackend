package com.portfolioBackend.portfolioBackend.dto;

import com.portfolioBackend.portfolioBackend.model.UserRole;
import lombok.Data;

@Data
public class AuthDTO {
    private String token;
    private String username;
    private UserRole userRole;
}
