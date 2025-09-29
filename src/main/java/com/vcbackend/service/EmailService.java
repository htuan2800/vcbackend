package com.vcbackend.service;

import java.util.concurrent.CompletableFuture;

public interface EmailService {
    public void sendVerificationEmail(String to, String token, String fullName);
    public CompletableFuture<Void> sendVerificationEmailWithAsync(String to, String token, String fullName);
    public String buildVerificationEmailTemplate(String fullName, String verificationLink);
}
