package com.newsletter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentRequest {
    private String text;
    private Long topicId;
    private LocalDateTime scheduledTime;
}
