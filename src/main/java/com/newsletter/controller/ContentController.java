package com.newsletter.controller;

import com.newsletter.dto.ContentRequest;
import com.newsletter.dto.ContentResponse;
import com.newsletter.model.Content;
import com.newsletter.model.Status;
import com.newsletter.repository.TopicRepository;
import com.newsletter.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    @Autowired
    private ContentService contentService;
    @Autowired
    private TopicRepository topicRepository;

    // Single content creation
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ContentRequest request) {
        return topicRepository.findById(request.getTopicId())
                .<ResponseEntity<?>>map(topic -> {
                    Content content = new Content(
                            null,
                            Status.PENDING,
                            request.getText(),
                            request.getScheduledTime(),
                            false,
                            topic
                    );
                    Content saved = contentService.save(content);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity
                        .status(404)
                        .body(Map.of("error", "Topic not found")));
    }

    // Bulk content creation
    @PostMapping("/bulk")
    public ResponseEntity<List<ContentResponse>> createBulk(@RequestBody List<ContentRequest> requests) {
        List<Content> contentsToSave = new ArrayList<>();
        List<ContentResponse> responses = new ArrayList<>();

        for (ContentRequest req : requests) {
            topicRepository.findById(req.getTopicId()).ifPresentOrElse(topic -> {
                contentsToSave.add(new Content(
                        null,
                        Status.PENDING,
                        req.getText(),
                        req.getScheduledTime(),
                        false,
                        topic
                ));
                responses.add(new ContentResponse(req.getText(), req.getTopicId(), "QUEUED_FOR_SAVE"));
            }, () -> responses.add(new ContentResponse(req.getText(), req.getTopicId(), "TOPIC_NOT_FOUND")));
        }

        if (!contentsToSave.isEmpty()) {
            contentService.saveAll(contentsToSave);
            responses.forEach(r -> {
                if ("QUEUED_FOR_SAVE".equals(r.getStatus())) {
                    r.setStatus("SAVED");
                }
            });
        }

        return ResponseEntity.ok(responses);
    }
}
