package com.newsletter.strategy.dispatch;

import com.newsletter.model.Content;
import com.newsletter.service.ContentPublisher;
import com.newsletter.service.EmailServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Dispatches content using the in-memory scheduler (default mode).
 */
@Component
public class DefaultDispatchStrategy implements ContentDispatchStrategy {

    @Autowired
    private ContentPublisher contentPublisher;

    @Autowired
    private EmailServiceFactory emailServiceFactory;

    @Value("${newsletter.email.sender}")
    private String emailSenderType;

    @Override
    public void dispatch(Content content) {
        contentPublisher.notifySubscribers(content, emailServiceFactory.getEmailSender(emailSenderType));
        System.out.println("Sent via Default strategy: " + content.getText());
    }
}

