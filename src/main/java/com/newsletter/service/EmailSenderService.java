package com.newsletter.service;

/**
 * Strategy Interface for sending emails.
 */
public interface EmailSenderService {
    void sendEmail(String to, String subject, String body);
}
