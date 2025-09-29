package com.vcbackend.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailServiceImp implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async("emailTaskExecutor")
    @Override
    public CompletableFuture<Void> sendVerificationEmailWithAsync(String to, String token, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject("Xác nhận tài khoản của bạn");
            
            String verificationLink = baseUrl + "/verify-email?token=" + token;
            String htmlContent = buildVerificationEmailTemplate(fullName, verificationLink);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);

            return CompletableFuture.completedFuture(null);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    public void sendVerificationEmail(String to, String token, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject("Xác nhận tài khoản của bạn");
            
            String verificationLink = baseUrl + "/verify-email?token=" + token;
            String htmlContent = buildVerificationEmailTemplate(fullName, verificationLink);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    public String buildVerificationEmailTemplate(String fullName, String verificationLink) {
         return """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: #f4f4f4; padding: 20px; text-align: center;">
                    <h1 style="color: #333;">Xác nhận tài khoản</h1>
                </div>
                <div style="padding: 20px;">
                    <h2>Xin chào %s,</h2>
                    <p>Cảm ơn bạn đã đăng ký tài khoản. Vui lòng click vào nút bên dưới để xác nhận email của bạn:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #007bff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">
                            Xác nhận Email
                        </a>
                    </div>
                    <p>Hoặc copy link này vào trình duyệt:</p>
                    <p style="word-break: break-all; color: #666;">%s</p>
                    <p><strong>Lưu ý:</strong> Link này sẽ hết hạn sau 24 giờ.</p>
                </div>
            </body>
            </html>
            """.formatted(fullName, verificationLink, verificationLink);
    }

}
