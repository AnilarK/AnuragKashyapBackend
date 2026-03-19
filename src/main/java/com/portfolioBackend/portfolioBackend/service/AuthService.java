package com.portfolioBackend.portfolioBackend.service;

import com.portfolioBackend.portfolioBackend.model.User;
import com.portfolioBackend.portfolioBackend.repository.UserRepository;
import com.portfolioBackend.portfolioBackend.security.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, OtpService otpService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Request OTP for email login. Sends OTP to the given email.
     */
    public void requestEmailOtp(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Invalid email");
        }
        otpService.sendOtp(email.trim().toLowerCase());
    }

    /**
     * Verify OTP and login. Creates user if not exists, returns JWT.
     */
    public String verifyEmailOtpAndLogin(String email, String otp) {
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
        return jwtUtil.generateToken(user);
    }
}
