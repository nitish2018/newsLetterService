package com.newsletter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory for creating different EmailSenderService instances.
 */
@Component
public class EmailServiceFactory {

    private final GmailEmailSender gmailSender;

    @Autowired
    public EmailServiceFactory(GmailEmailSender gmailSender) {
        this.gmailSender = gmailSender;
    }

    public EmailSenderService getEmailSender(String type) {
        if ("gmail".equalsIgnoreCase(type)) return gmailSender;
        throw new IllegalArgumentException("Unsupported email sender type");
    }
}
