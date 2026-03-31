package com.portfolioBackend.portfolioBackend.dto;

import com.portfolioBackend.portfolioBackend.model.UserRole;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserDTO {

    private String email;
    private String name;

    @Builder.Default
    private UserRole userRoles = UserRole.USER;
}