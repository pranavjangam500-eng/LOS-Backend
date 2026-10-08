package com.bank.los.otp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service("otpEmailService")
public class OtpEmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.from:${SMTP_FROM:OTP Auth Service <noreply@losplatform.com>}}")
    private String fromAddress;

    @Value("${spring.mail.username:${SMTP_USERNAME:${SMTP_USER:}}}")
    private String mailUsername;

    public OtpEmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public void sendOtpEmail(String recipientEmail, String otp) {
        log.info("[EMAIL] Dispatching OTP email to recipient: {}", recipientEmail);

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null || mailUsername == null || mailUsername.isBlank() || mailUsername.contains("your_email")) {
            log.warn("[EMAIL SIMULATION] SMTP not configured in environment. Simulated dispatch to {}", recipientEmail);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromAddress);
            helper.setTo(recipientEmail);
            helper.setSubject("Your Verification Code");

            String htmlContent = """
                <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; max-width: 500px; margin: 0 auto; background: #0f172a; color: #f8fafc; padding: 32px 24px; border-radius: 16px; border: 1px solid #1e293b;">
                  <div style="text-align: center; margin-bottom: 24px;">
                    <div style="display: inline-block; padding: 6px 14px; background: rgba(59, 130, 246, 0.15); border: 1px solid rgba(59, 130, 246, 0.3); border-radius: 20px; font-size: 12px; font-weight: 600; color: #60a5fa; text-transform: uppercase; letter-spacing: 0.05em;">
                      Security Verification
                    </div>
                  </div>
                  <h2 style="color: #ffffff; font-size: 22px; font-weight: 700; text-align: center; margin: 0 0 12px 0;">Your Verification Code</h2>
                  <p style="color: #94a3b8; font-size: 14px; text-align: center; margin: 0 0 24px 0;">Use the following single-use code to authenticate your email address:</p>
                  
                  <div style="background: rgba(15, 23, 42, 0.8); border: 1px solid #334155; border-radius: 12px; padding: 20px; text-align: center; margin-bottom: 24px;">
                    <span style="font-family: 'Courier New', monospace; font-size: 36px; font-weight: 800; letter-spacing: 8px; color: #38bdf8;">%s</span>
                  </div>
                  
                  <p style="color: #64748b; font-size: 13px; text-align: center; line-height: 1.5; margin: 0 0 16px 0;">
                    ⏳ This code will expire in <strong>5 minutes</strong>.<br/>
                    🔒 Single-use only. Never share this code with anyone.
                  </p>
                  <hr style="border: none; border-top: 1px solid #1e293b; margin: 20px 0;" />
                  <p style="color: #475569; font-size: 11px; text-align: center; margin: 0;">If you didn't request this verification code, you can safely ignore this email.</p>
                </div>
            """.formatted(otp);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("[SMTP SUCCESS] Email successfully dispatched to {}", recipientEmail);

        } catch (MessagingException | MailException e) {
            log.error("[SMTP ERROR] Failed to send email via SMTP: {}", e.getMessage());
            throw new RuntimeException("SMTP delivery failed: " + e.getMessage(), e);
        }
    }
}
