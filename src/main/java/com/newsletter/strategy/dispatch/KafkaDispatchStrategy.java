package com.newsletter.strategy.dispatch;

import com.newsletter.model.Content;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Dispatches content asynchronously through Kafka.
 * (Requires Spring Kafka dependency)
 */
@Component
public class KafkaDispatchStrategy implements ContentDispatchStrategy {

    private final KafkaTemplate<String, Content> kafkaTemplate;

    public KafkaDispatchStrategy(KafkaTemplate<String, Content> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void dispatch(Content content) {
        kafkaTemplate.send("newsletter.send", content);
        System.out.println("Queued via Kafka strategy: " + content.getId());
    }
}

