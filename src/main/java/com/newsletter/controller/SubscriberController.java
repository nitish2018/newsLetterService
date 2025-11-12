package com.newsletter.controller;

import com.newsletter.dto.SubscriberRequest;
import com.newsletter.model.Subscriber;
import com.newsletter.repository.TopicRepository;
import com.newsletter.service.SubscriberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/subscribers")
public class SubscriberController {

    @Autowired
    private SubscriberService subscriberService;
    @Autowired
    private TopicRepository topicRepo;

    // Single subscriber creation
    @PostMapping
    public ResponseEntity<?> createSingle(@RequestBody SubscriberRequest request) {
        return topicRepo.findById(request.getTopicId())
                .<ResponseEntity<?>>map(topic -> {
                    try {
                        Subscriber saved = subscriberService.save(
                                new Subscriber(null, request.getEmail(), topic)
                        );
                        return ResponseEntity.ok(Map.of(
                                "email", saved.getEmail(),
                                "topicId", saved.getTopic().getId(),
                                "status", "SUBSCRIBED"
                        ));
                    } catch (DataIntegrityViolationException e) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                                "email", request.getEmail(),
                                "topicId", request.getTopicId(),
                                "status", "ALREADY_EXISTS"
                        ));
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                                "email", request.getEmail(),
                                "topicId", request.getTopicId(),
                                "status", "FAILED",
                                "error", e.getMessage()
                        ));
                    }
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "email", request.getEmail(),
                        "topicId", request.getTopicId(),
                        "status", "TOPIC_NOT_FOUND"
                )));
    }

    // Bulk subscriber creation
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> createBulk(@RequestBody List<SubscriberRequest> requests) {
        Set<String> uniqueKeys = new HashSet<>();
        List<Subscriber> toSave = new ArrayList<>();
        List<Map<String, Object>> details = new ArrayList<>();

        for (SubscriberRequest req : requests) {
            String key = req.getEmail() + "-" + req.getTopicId();

            if (!uniqueKeys.add(key)) {
                details.add(Map.of(
                        "email", req.getEmail(),
                        "topicId", req.getTopicId(),
                        "status", "DUPLICATE_IN_REQUEST"
                ));
                continue;
            }

            topicRepo.findById(req.getTopicId()).ifPresentOrElse(topic -> {
                toSave.add(new Subscriber(null, req.getEmail(), topic));
            }, () -> details.add(Map.of(
                    "email", req.getEmail(),
                    "topicId", req.getTopicId(),
                    "status", "TOPIC_NOT_FOUND"
            )));
        }

        if (toSave.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "NO_VALID_SUBSCRIBERS",
                    "message", "No valid subscribers to save.",
                    "details", details
            ));
        }

        try {
            List<Subscriber> savedList = subscriberService.saveAll(toSave);
            for (Subscriber s : savedList) {
                details.add(Map.of(
                        "email", s.getEmail(),
                        "topicId", s.getTopic().getId(),
                        "status", "SUBSCRIBED"
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "All subscribers saved successfully.",
                    "subscribedCount", savedList.size(),
                    "details", details
            ));

        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "status", "PARTIAL_FAILURE",
                    "message", "Some subscribers already exist or violated constraints. None were saved.",
                    "details", details
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "FAILED",
                    "message", e.getMessage(),
                    "details", details
            ));
        }
    }
}
