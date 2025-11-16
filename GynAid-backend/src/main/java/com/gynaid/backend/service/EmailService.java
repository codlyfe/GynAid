package com.gynaid.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Enterprise-grade email service for GynAid
 * Supports both plain text and HTML emails with proper error handling
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.email.from-name:GynAid}")
    private String fromName;

    /**
     * Send a simple email message
     */
    public void sendEmail(EmailVerificationService.EmailMessage message) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(fromEmail);
            email.setTo(message.getTo());
            email.setSubject(message.getSubject());
            email.setText(message.getHtmlContent()); // Using HTML content as text for now
            
            mailSender.send(email);
            
            log.info("Email sent successfully to: {}", message.getTo());
            
        } catch (MailException e) {
            log.error("Failed to send email to: {}", message.getTo(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send HTML email with proper formatting
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = isHtml
            
            mailSender.send(message);
            
            log.info("HTML email sent successfully to: {}", to);
            
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    /**
     * Send email with multiple recipients
     */
    public void sendBulkEmail(String[] to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            log.info("Bulk email sent successfully to {} recipients", to.length);
            
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send bulk email", e);
            throw new RuntimeException("Failed to send bulk email", e);
        }
    }
}