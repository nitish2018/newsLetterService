package com.newsletter.service;

import com.newsletter.model.Subscriber;
import com.newsletter.repository.SubscriberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriberService {
    @Autowired
    private SubscriberRepository subscriberRepository;
    public Subscriber save(Subscriber subscriber) {
        subscriberRepository.save(subscriber);
        return subscriber;
    }
    public List<Subscriber> saveAll(List<Subscriber> subscribers) {
        return subscriberRepository.saveAll(subscribers);
    }
}
