package com.newsletter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscriberResponse {
    private String email;
    private Long topicId;
    private String status;
}
