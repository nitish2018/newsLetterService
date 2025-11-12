package com.newsletter.service;

import com.newsletter.model.Topic;
import com.newsletter.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TopicService {
    @Autowired
    private TopicRepository topicRepository;
    public Topic save(Topic topic) { return topicRepository.save(topic); }
}
