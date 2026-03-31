package com.portfolioBackend.portfolioBackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class SendGridEmailService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Value("${SENDGRID_API_KEY:}")
    private String apiKey;

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    public void sendTextEmail(String from, String to, String subject, String text) {
        if (!isConfigured()) {
            throw new IllegalStateException("SENDGRID_API_KEY is not set");
        }
        String bodyJson = buildSendGridJson(from, to, subject, text);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.sendgrid.com/v3/mail/send"))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            // Success is 202 Accepted
            if (response.statusCode() != 202) {
                throw new IllegalStateException("SendGrid send failed: HTTP " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new IllegalStateException("SendGrid send failed: " + e.getMessage(), e);
        }
    }

    private String buildSendGridJson(String from, String to, String subject, String text) {
        // Minimal JSON with only text/plain content.
        return "{"
                + "\"personalizations\":[{\"to\":[{\"email\":\"" + esc(to) + "\"}]}],"
                + "\"from\":{\"email\":\"" + esc(from) + "\"},"
                + "\"subject\":\"" + esc(subject) + "\","
                + "\"content\":[{\"type\":\"text/plain\",\"value\":\"" + esc(text) + "\"}]"
                + "}";
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}

