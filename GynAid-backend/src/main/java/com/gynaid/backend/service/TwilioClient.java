package com.gynaid.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 * Twilio client wrapper for SMS functionality
 */
@Slf4j
@Component
public class TwilioClient {

    @Value("${app.sms.twilio.account-sid}")
    private String accountSid;

    @Value("${app.sms.twilio.auth-token}")
    private String authToken;

    @Value("${app.sms.twilio.phone-number}")
    private String fromPhoneNumber;

    /**
     * Initialize Twilio client
     */
    public void initialize() {
        try {
            Twilio.init(accountSid, authToken);
            log.info("Twilio client initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Twilio client", e);
        }
    }

    /**
     * Send SMS message
     */
    public boolean sendSMS(String to, String message) {
        try {
            Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(fromPhoneNumber),
                message
            ).create();

            log.info("SMS sent successfully to: {}", to);
            return true;

        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", to, e);
            return false;
        }
    }

    /**
     * Send SMS with delivery status callback
     */
    public boolean sendSMSWithStatusCallback(String to, String message, String statusCallbackUrl) {
        try {
            Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(fromPhoneNumber),
                message
            ).setStatusCallback(statusCallbackUrl)
            .create();

            log.info("SMS sent with status callback to: {}", to);
            return true;

        } catch (Exception e) {
            log.error("Failed to send SMS with callback to: {}", to, e);
            return false;
        }
    }
}