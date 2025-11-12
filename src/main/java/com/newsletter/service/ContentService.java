package com.newsletter.service;

import com.newsletter.model.Content;
import com.newsletter.repository.ContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentService {
    @Autowired
    private ContentRepository contentRepository;
    public Content save(Content content) { return contentRepository.save(content); }
    public void saveAll(List<Content> contents) {
        contentRepository.saveAll(contents);
    }
}
