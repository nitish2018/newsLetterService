package com.newsletter.dto;

import lombok.Data;

@Data
public class SubscriberRequest {
    private String email;
    private Long topicId;
}
