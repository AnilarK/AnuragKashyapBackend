package com.portfolioBackend.portfolioBackend.controller;

import com.portfolioBackend.portfolioBackend.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${server.port:8080}")
    private String serverPort;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/google/url")
    public ResponseEntity<Map<String, String>> googleLoginUrl(
            @RequestParam(value = "baseUrl", required = false) String baseUrl) {
        String base = baseUrl != null ? baseUrl : "http://localhost:" + serverPort;
        String url = base + "/oauth2/authorization/google";
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/login/email/request-otp")
    public ResponseEntity<?> requestEmailOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "email is required"));
        }
        try {
            authService.requestEmailOtp(email);
            return ResponseEntity.ok(Map.of("message", "OTP sent to your email"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login/email/verify-otp")
    public ResponseEntity<?> verifyEmailOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");
        if (email == null || email.isBlank() || otp == null || otp.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "email and otp are required"));
        }
        try {
            String token = authService.verifyEmailOtpAndLogin(email, otp);
            return ResponseEntity.ok(Map.of("token", token, "type", "Bearer"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
