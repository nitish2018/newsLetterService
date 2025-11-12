package com.newsletter.service;

import com.newsletter.model.Content;
import com.newsletter.model.Status;
import com.newsletter.repository.ContentRepository;
import com.newsletter.strategy.dispatch.ContentDispatchStrategy;
import com.newsletter.strategy.dispatch.DispatchStrategyFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContentDispatcher {

    private final ContentRepository contentRepository;
    private final DispatchStrategyFactory dispatchStrategyFactory;

    public ContentDispatcher(ContentRepository contentRepository,
                             DispatchStrategyFactory dispatchStrategyFactory) {
        this.contentRepository = contentRepository;
        this.dispatchStrategyFactory = dispatchStrategyFactory;
    }

    /**
     * Dispatches a single content item via the chosen strategy.
     */
    public void dispatchContent(Content content) {
        ContentDispatchStrategy contentDispatchStrategy = dispatchStrategyFactory.getStrategy();
        contentDispatchStrategy.dispatch(content);
    }

    /**
     * Checks all PENDING content within the next ±30 seconds of current time
     * and dispatches them if eligible.
     */
    public void checkAndDispatch() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusSeconds(30);
        LocalDateTime end = now.plusSeconds(30);

        // Range query -> O(log n) search time
        List<Content> readyContent = contentRepository.findByStatusAndScheduledTimeBetween(
                Status.PENDING, start, end
        );

        for (Content content : readyContent) {
            if (!content.isSent() && !Status.FAILED.equals(content.getStatus())) {
                try {
                    dispatchContent(content);
                    content.setSent(true);
                    content.setStatus(Status.SENT);
                } catch (Exception exception) {
                    content.setSent(false);
                    content.setStatus(Status.FAILED);
                }
                contentRepository.save(content);
            }
        }
    }
}
