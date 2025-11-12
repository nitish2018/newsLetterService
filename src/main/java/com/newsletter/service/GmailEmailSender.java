package com.newsletter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Concrete implementation using Spring's JavaMailSender.
 */
@Service
public class GmailEmailSender implements EmailSenderService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${newsletter.email.from}")
    private String from;  // Injected from properties

    @Override
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        System.out.println("Sent to: " + to);
    }
}
