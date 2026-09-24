package com.bank.los.bank.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final org.springframework.beans.factory.ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:noreply@losplatform.com}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp, String firstName) {
        String greeting = (firstName != null && !firstName.isBlank()) ? "Hello " + firstName : "Hello";
        String body = String.format(
                "%s,\n\nYour One-Time Password (OTP) for LOS Platform login is: %s\n\nThis OTP is valid for 5 minutes.\nIf you did not request this login, please contact your administrator immediately.\n\nRegards,\nLOS Platform Security Team",
                greeting, otp);

        sendEmail(toEmail, "LOS Platform — 2FA Login OTP: " + otp, body);
        log.info("[MAIL STUB] 2FA OTP for {} → {} (enable SMTP to send real email)", toEmail, otp);
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken, String empNo) {
        String body = String.format(
                "Hello,\n\nA password reset was requested for your LOS account (EmpNo: %s).\n\nYour reset token is:\n%s\n\nThis token is valid for 15 minutes.\nIf you did not request a password reset, please contact your security administrator.\n\nRegards,\nLOS Platform Security Team",
                empNo, resetToken);

        sendEmail(toEmail, "LOS Platform — Password Reset Instructions", body);
        log.info("[MAIL STUB] Password reset instructions sent to {} for empNo={}", toEmail, empNo);
    }

    private void sendEmail(String to, String subject, String text) {
        if (!mailEnabled) {
            return;
        }
        try {
            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            if (mailSender == null) {
                log.warn("MailSender bean not available; email to {} not sent", to);
                return;
            }
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
            log.info("Email sent successfully to {}", to);
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }
}
