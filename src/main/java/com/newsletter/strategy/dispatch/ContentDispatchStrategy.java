package com.newsletter.strategy.dispatch;

import com.newsletter.model.Content;

/**
 * Strategy interface for dispatching newsletter content.
 */
public interface ContentDispatchStrategy {
    void dispatch(Content content);
}

