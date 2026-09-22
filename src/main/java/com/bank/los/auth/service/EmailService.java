package com.bank.los.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Email service for OTP and password-reset emails.
 *
 * Currently STUBBED — OTP is returned in API response until SMTP credentials
 * are configured via environment variables:
 *   MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD
 *
 * To activate: set app.mail.enabled=true in application.yml after adding SMTP creds.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:noreply@losplatform.com}")
    private String fromAddress;

    /**
     * Send 2FA OTP email to user.
     * No-op if mail is disabled (SMTP not configured yet).
     */
    public void sendOtpEmail(String toEmail, String otp, String userName) {
        if (!mailEnabled) {
            log.info("[MAIL STUB] 2FA OTP for {} → {} (enable SMTP to send real email)", toEmail, otp);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Your Login OTP - LOS Platform");
            helper.setText(buildOtpEmailHtml(userName, otp), true);
            mailSender.send(message);
            log.info("2FA OTP email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Send password reset link/token email.
     * No-op if mail is disabled.
     */
    public void sendPasswordResetEmail(String toEmail, String rawToken, String userName) {
        if (!mailEnabled) {
            log.info("[MAIL STUB] Password reset token for {} → {} (enable SMTP to send real email)", toEmail, rawToken);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - LOS Platform");
            helper.setText(buildResetEmailHtml(userName, rawToken), true);
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildOtpEmailHtml(String userName, String otp) {
        return """
                <html><body style="font-family:Arial,sans-serif;background:#f5f5f5;padding:20px;">
                  <div style="max-width:480px;margin:auto;background:#fff;border-radius:8px;padding:32px;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                    <h2 style="color:#1a237e;">LOS Platform — Login Verification</h2>
                    <p>Dear <strong>%s</strong>,</p>
                    <p>Your one-time password (OTP) for login verification is:</p>
                    <div style="font-size:36px;font-weight:bold;letter-spacing:8px;color:#1a237e;text-align:center;padding:16px;background:#e8eaf6;border-radius:6px;margin:16px 0;">%s</div>
                    <p style="color:#d32f2f;"><strong>This OTP is valid for 5 minutes only. Do not share it with anyone.</strong></p>
                    <p style="color:#666;font-size:12px;">If you did not attempt to log in, please contact your administrator immediately.</p>
                  </div>
                </body></html>
                """.formatted(userName, otp);
    }

    private String buildResetEmailHtml(String userName, String token) {
        return """
                <html><body style="font-family:Arial,sans-serif;background:#f5f5f5;padding:20px;">
                  <div style="max-width:480px;margin:auto;background:#fff;border-radius:8px;padding:32px;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                    <h2 style="color:#1a237e;">LOS Platform — Password Reset</h2>
                    <p>Dear <strong>%s</strong>,</p>
                    <p>Use the token below to reset your password. This token expires in <strong>15 minutes</strong>.</p>
                    <div style="font-size:14px;font-family:monospace;word-break:break-all;color:#1a237e;padding:12px;background:#e8eaf6;border-radius:6px;margin:16px 0;">%s</div>
                    <p style="color:#d32f2f;"><strong>Do not share this token with anyone.</strong></p>
                    <p style="color:#666;font-size:12px;">If you did not request a password reset, please ignore this email.</p>
                  </div>
                </body></html>
                """.formatted(userName, token);
    }
}
