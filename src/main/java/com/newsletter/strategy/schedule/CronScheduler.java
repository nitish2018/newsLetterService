package com.newsletter.strategy.schedule;

import com.newsletter.service.ContentDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component("cronScheduler")
public class CronScheduler implements SchedulerStrategy {

    @Autowired
    private ContentDispatcher dispatcher;

    @Override
    @Scheduled(cron = "0 */5 * * * *") // every 5 minutes
    public void start() {
        dispatcher.checkAndDispatch();
    }
}
