package com.portfolioBackend.portfolioBackend.service.impl;

import com.portfolioBackend.portfolioBackend.model.OtpRecord;
import com.portfolioBackend.portfolioBackend.repository.OtpRecordRepository;
import com.portfolioBackend.portfolioBackend.service.IOtpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService implements IOtpService {

    private static final String OTP_CHARS = "0123456789";
    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALID_MINUTES = 10;

    private final OtpRecordRepository otpRecordRepository;
    private final EmailService emailService;

    @Value("${app.mail.from:}")
    private String mailFrom;

    @Value("${app.mail.from-name:}")
    private String mailFromName;

    public OtpService(OtpRecordRepository otpRecordRepository,
                      EmailService emailService) {
        this.otpRecordRepository = otpRecordRepository;
        this.emailService = emailService;
    }

    @Override
    public void sendOtp(String email) {
        String otp = generateOtp();
        Instant expiresAt = Instant.now().plusSeconds(TimeUnit.MINUTES.toSeconds(OTP_VALID_MINUTES));
        OtpRecord record = new OtpRecord(email, otp, expiresAt);
        otpRecordRepository.save(record);


        String fromEmail = mailFrom;
        String fromName = mailFromName;
        String subject = "OTP";
        String html = "<h1>Your OTP is " + otp + "</h1>";

        emailService.sendHtmlEmail(fromName, fromEmail, email, subject, html);
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        return otpRecordRepository.findByEmailAndOtpAndUsedFalse(email, otp)
                .filter(r -> !r.isExpired())
                .map(record -> {
                    record.setUsed(true);
                    otpRecordRepository.save(record);
                    return true;
                })
                .orElse(false);
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(OTP_CHARS.charAt(random.nextInt(OTP_CHARS.length())));
        }
        return sb.toString();
    }
}
