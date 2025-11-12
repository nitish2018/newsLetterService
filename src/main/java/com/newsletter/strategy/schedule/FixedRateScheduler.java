package com.newsletter.strategy.schedule;

import com.newsletter.service.ContentDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component("fixedRateScheduler")
public class FixedRateScheduler implements SchedulerStrategy {

    @Autowired
    private ContentDispatcher dispatcher;

    @Override
    @Scheduled(fixedRate = 10000) // every minute
    public void start() {
        dispatcher.checkAndDispatch();
    }
}
