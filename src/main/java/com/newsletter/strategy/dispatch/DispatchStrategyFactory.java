package com.newsletter.strategy.dispatch;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory that selects the appropriate content dispatch strategy
 * at runtime based on configuration.
 */
@Component
public class DispatchStrategyFactory {

    @Value("${newsletter.dispatch.strategy:scheduler}")
    private String activeStrategy;

    @Autowired
    private DefaultDispatchStrategy schedulerStrategy;

    @Autowired
    private KafkaDispatchStrategy kafkaStrategy;

    public ContentDispatchStrategy getStrategy() {
        if (Type.KAFKA.name().equalsIgnoreCase(activeStrategy)) {
            return kafkaStrategy;
        }
        return schedulerStrategy;
    }
}
