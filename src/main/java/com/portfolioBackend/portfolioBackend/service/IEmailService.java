package com.portfolioBackend.portfolioBackend.service;

public interface IEmailService {

    void sendHtmlEmail(String fromName, String fromEmail, String toEmail, String subject, String htmlContent);

}
