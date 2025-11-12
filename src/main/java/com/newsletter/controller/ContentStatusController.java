package com.newsletter.controller;

import com.newsletter.model.Content;
import com.newsletter.repository.ContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/status")
public class ContentStatusController {

    @Autowired
    private ContentRepository contentRepository;

    /**
     * Get all content with their sent status
     */
    @GetMapping
    public ResponseEntity<List<Content>> getAllContentStatus() {
        return ResponseEntity.ok(contentRepository.findAll());
    }

    /**
     * Get status for a specific content by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Content> getContentStatusById(@PathVariable Long id) {
        return contentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get only unsent content (pending newsletters)
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Content>> getPendingContent() {
        return ResponseEntity.ok(contentRepository.findAll()
                .stream()
                .filter(content -> !content.isSent())
                .toList());
    }

    /**
     * Get only sent content
     */
    @GetMapping("/sent")
    public ResponseEntity<List<Content>> getSentContent() {
        return ResponseEntity.ok(contentRepository.findAll()
                .stream()
                .filter(Content::isSent)
                .toList());
    }
}
