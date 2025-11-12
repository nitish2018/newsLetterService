package com.newsletter.dto;

import lombok.Data;

@Data
public class CreateContentRequest {
    private String text;
    private Long topicId;
    private String scheduledTime;
}
