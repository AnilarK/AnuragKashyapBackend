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
public class BrevoEmailService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Value("${app.brevo.api-key:}")
    private String apiKey;

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    public void sendHtmlEmail(String fromName, String fromEmail, String toEmail, String subject, String htmlContent) {
        if (!isConfigured()) {
            throw new IllegalStateException("app.brevo.api-key is not set");
        }
        String bodyJson = buildBrevoJson(fromName, fromEmail, toEmail, subject, htmlContent);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .timeout(Duration.ofSeconds(20))
                .header("api-key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Brevo send failed: HTTP " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Brevo send failed: " + e.getMessage(), e);
        }
    }

    private String buildBrevoJson(String fromName, String fromEmail, String toEmail, String subject, String htmlContent) {
        return "{"
                + "\"sender\":{\"name\":\"" + esc(fromName) + "\",\"email\":\"" + esc(fromEmail) + "\"},"
                + "\"to\":[{\"email\":\"" + esc(toEmail) + "\"}],"
                + "\"subject\":\"" + esc(subject) + "\","
                + "\"htmlContent\":\"" + esc(htmlContent) + "\""
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

