package com.portfolioBackend.portfolioBackend.service;

import com.portfolioBackend.portfolioBackend.model.OtpRecord;
import com.portfolioBackend.portfolioBackend.repository.OtpRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private static final String OTP_CHARS = "0123456789";
    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALID_MINUTES = 10;
    private static final String DEFAULT_FROM = "noreply@example.com";
    private static final String DEFAULT_FROM_NAME = "PortfolioBackend";

    private final OtpRecordRepository otpRecordRepository;
    private final BrevoEmailService brevoEmailService;

    @Value("${app.mail.from:}")
    private String mailFrom;

    @Value("${app.mail.from-name:}")
    private String mailFromName;

    public OtpService(OtpRecordRepository otpRecordRepository,
                      BrevoEmailService brevoEmailService) {
        this.otpRecordRepository = otpRecordRepository;
        this.brevoEmailService = brevoEmailService;
    }

    public void sendOtp(String email) {
        String otp = generateOtp();
        Instant expiresAt = Instant.now().plusSeconds(TimeUnit.MINUTES.toSeconds(OTP_VALID_MINUTES));
        OtpRecord record = new OtpRecord(email, otp, expiresAt);
        otpRecordRepository.save(record);

        if (!brevoEmailService.isConfigured()) {
            throw new IllegalStateException("Brevo is not configured. Set app.brevo.api-key (or env BREVO_API_KEY).");
        }

        String fromEmail = StringUtils.hasText(mailFrom) ? mailFrom : DEFAULT_FROM;
        String fromName = StringUtils.hasText(mailFromName) ? mailFromName : DEFAULT_FROM_NAME;
        String subject = "OTP";
        String html = "<h1>Your OTP is " + otp + "</h1>";

        brevoEmailService.sendHtmlEmail(fromName, fromEmail, email, subject, html);
    }

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
