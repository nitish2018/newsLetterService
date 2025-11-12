package com.newsletter.repository;

import com.newsletter.model.Subscriber;
import com.newsletter.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    List<Subscriber> findByTopic(Topic topic);
}
