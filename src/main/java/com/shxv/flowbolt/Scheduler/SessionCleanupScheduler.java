package com.shxv.flowbolt.Scheduler;

import com.shxv.flowbolt.Auth.Repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class SessionCleanupScheduler {

    @Autowired
    SessionRepository sessionRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupOldSessions() {
        Instant cutoff = Instant.now().minus(7, ChronoUnit.DAYS);

        sessionRepository.deleteExpiredSessions(cutoff)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        null,
                        error -> System.err.println("Error cleaning old sessions: " + error.getMessage())
                );
    }
}

