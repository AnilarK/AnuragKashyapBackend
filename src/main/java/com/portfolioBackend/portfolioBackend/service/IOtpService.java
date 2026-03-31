package com.portfolioBackend.portfolioBackend.service;

public interface IOtpService {
    void sendOtp(String email);
    boolean verifyOtp(String email, String otp);
}
