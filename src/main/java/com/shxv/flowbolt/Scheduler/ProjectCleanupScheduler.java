package com.shxv.flowbolt.Scheduler;

import com.shxv.flowbolt.Project.Service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectCleanupScheduler {

    private final ProjectService projectService;

    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupOldProjects() {

        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);

        projectService.getAllProjects()
                .filter(project ->
                        project.getUpdatedAt() != null &&
                                project.getUpdatedAt().isBefore(cutoff)
                )
                .flatMap(project ->
                        projectService.deleteProjectById(project.getId())
                                .doOnSuccess(unused ->
                                        log.info("Deleted project: {}", project.getId())
                                )
                                .doOnError(error ->
                                        log.error("Error deleting project {}: {}",
                                                project.getId(),
                                                error.getMessage())
                                )
                )
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }
}