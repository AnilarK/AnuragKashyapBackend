package com.portfolioBackend.portfolioBackend.service;

import com.portfolioBackend.portfolioBackend.model.OtpRecord;
import com.portfolioBackend.portfolioBackend.repository.OtpRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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

    private final OtpRecordRepository otpRecordRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.mail.from:}")
    private String mailFrom;

    public OtpService(OtpRecordRepository otpRecordRepository, JavaMailSender mailSender) {
        this.otpRecordRepository = otpRecordRepository;
        this.mailSender = mailSender;
    }

    public void sendOtp(String email) {
        String otp = generateOtp();
        Instant expiresAt = Instant.now().plusSeconds(TimeUnit.MINUTES.toSeconds(OTP_VALID_MINUTES));
        OtpRecord record = new OtpRecord(email, otp, expiresAt);
        otpRecordRepository.save(record);

        SimpleMailMessage message = new SimpleMailMessage();
        // SendGrid SMTP uses username="apikey" (not an email), so support explicit from.
        String from = StringUtils.hasText(mailFrom)
                ? mailFrom
                : (StringUtils.hasText(mailUsername) && mailUsername.contains("@") ? mailUsername : DEFAULT_FROM);

        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Your login code");
        message.setText("Your one-time login code is: " + otp + "\n\nIt is valid for " + OTP_VALID_MINUTES + " minutes.");
        mailSender.send(message);
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
