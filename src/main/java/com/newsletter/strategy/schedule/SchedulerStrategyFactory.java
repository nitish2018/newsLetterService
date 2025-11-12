package com.newsletter.strategy.schedule;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SchedulerStrategyFactory {

    private final SchedulerStrategy fixedRateScheduler;
    private final SchedulerStrategy cronScheduler;

    @Value("${newsletter.scheduler.strategy:fixedRate}")
    private String schedulerType;

    public SchedulerStrategyFactory(
            @Qualifier("fixedRateScheduler") SchedulerStrategy fixedRateScheduler,
            @Qualifier("cronScheduler") SchedulerStrategy cronScheduler) {
        this.fixedRateScheduler = fixedRateScheduler;
        this.cronScheduler = cronScheduler;
    }

    public SchedulerStrategy getScheduler() {
        return Type.CRON.name().equalsIgnoreCase(schedulerType)
                ? cronScheduler
                : fixedRateScheduler;
    }
}
