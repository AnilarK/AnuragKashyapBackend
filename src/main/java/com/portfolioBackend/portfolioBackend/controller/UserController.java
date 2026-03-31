package com.portfolioBackend.portfolioBackend.controller;

import com.portfolioBackend.portfolioBackend.dto.UserDTO;
import com.portfolioBackend.portfolioBackend.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", user.getId());
        dto.put("email", user.getEmail());
        dto.put("name", user.getName());
        dto.put("pictureUrl", user.getPictureUrl());
        dto.put("roles", user.getUserRoles());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/user")
    public  ResponseEntity<?> createUser(@RequestBody UserDTO userDTO){


    }

}
