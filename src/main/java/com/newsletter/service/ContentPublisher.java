package com.newsletter.service;

import com.newsletter.model.*;
import com.newsletter.repository.SubscriberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Publishes content updates to subscribers of that topic.
 */
@Service
public class ContentPublisher {

    @Autowired
    private SubscriberRepository subscriberRepository;

    public void notifySubscribers(Content content, EmailSenderService emailSender) {
        List<Subscriber> subscribers = subscriberRepository.findByTopic(content.getTopic());
        for (Subscriber subscriber : subscribers) {
            sendEmail(emailSender, subscriber, content);
        }
    }

    @Async("dispatcherExecutor")
    private void sendEmail(EmailSenderService emailSender, Subscriber subscriber, Content content) {
        emailSender.sendEmail(
                subscriber.getEmail(),
                "Newsletter: " + content.getTopic().getName(),
                content.getText()
        );
    }
}
