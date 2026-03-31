package com.portfolioBackend.portfolioBackend.service;

import com.portfolioBackend.portfolioBackend.dto.AuthDTO;

public interface IAuthService {

    void requestEmailOtp(String email);
    AuthDTO verifyEmailOtpAndLogin(String email, String otp);

}
