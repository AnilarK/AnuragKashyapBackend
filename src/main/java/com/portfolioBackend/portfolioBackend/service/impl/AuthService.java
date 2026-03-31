package com.portfolioBackend.portfolioBackend.service.impl;

import com.portfolioBackend.portfolioBackend.dto.AuthDTO;
import com.portfolioBackend.portfolioBackend.model.User;
import com.portfolioBackend.portfolioBackend.repository.UserRepository;
import com.portfolioBackend.portfolioBackend.security.JwtUtil;
import com.portfolioBackend.portfolioBackend.service.IAuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, OtpService otpService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.jwtUtil = jwtUtil;
    }


    public void requestEmailOtp(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Invalid email");
        }
        otpService.sendOtp(email.trim().toLowerCase());
    }


    public AuthDTO verifyEmailOtpAndLogin(String email, String otp) {
        if (email == null || otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("Email and OTP are required");
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (!otpService.verifyOtp(normalizedEmail, otp.trim())) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> {
                    User newUser = User.fromEmail(normalizedEmail);
                    return userRepository.save(newUser);
                });
        String token =  jwtUtil.generateToken(user);


        AuthDTO response = new AuthDTO();
        response.setToken(token);
        response.setUserRole(user.getUserRoles());
        response.setUsername(user.getName());
        return response;
    }
}
