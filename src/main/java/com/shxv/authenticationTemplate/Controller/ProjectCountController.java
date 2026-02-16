package com.shxv.authenticationTemplate.Controller;

import com.shxv.authenticationTemplate.DTO.ProjectCountResponseDTO;
import com.shxv.authenticationTemplate.Service.ProjectCountService;
import com.shxv.authenticationTemplate.Util.ResponseEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/project-count")
@RequiredArgsConstructor
public class ProjectCountController {

    private final ProjectCountService projectCountService;

    @PostMapping("/{projectId}")
    public Mono<ResponseEnvelope<ProjectCountResponseDTO>> createProjectCount(@PathVariable UUID projectId) {
        return projectCountService.createProjectCount(projectId)
                .map(res -> ResponseEnvelope.<ProjectCountResponseDTO>builder()
                        .success(true)
                        .status(HttpStatus.CREATED.value())
                        .message("Project count created successfully")
                        .data(res)
                        .build());
    }

    @GetMapping("/{projectId}")
    public Mono<ResponseEnvelope<ProjectCountResponseDTO>> getProjectCount(@PathVariable UUID projectId) {
        return projectCountService.getProjectCount(projectId)
                .map(res -> ResponseEnvelope.<ProjectCountResponseDTO>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Project count fetched successfully")
                        .data(res)
                        .build());
    }

    @PutMapping("/{projectId}/increment")
    public Mono<ResponseEnvelope<Integer>> increment(@PathVariable UUID projectId) {
        return projectCountService.incrementCount(projectId)
                .map(res -> ResponseEnvelope.<Integer>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Ticket count incremented successfully")
                        .data(res)
                        .build());
    }

    @PutMapping("/{projectId}/decrement")
    public Mono<ResponseEnvelope<ProjectCountResponseDTO>> decrement(@PathVariable UUID projectId) {
        return projectCountService.decrementCount(projectId)
                .map(res -> ResponseEnvelope.<ProjectCountResponseDTO>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Ticket count decremented successfully")
                        .data(res)
                        .build());
    }

    @PutMapping("/{projectId}/set/{count}")
    public Mono<ResponseEnvelope<ProjectCountResponseDTO>> setCount(
            @PathVariable UUID projectId,
            @PathVariable Integer count
    ) {
        return projectCountService.setCount(projectId, count)
                .map(res -> ResponseEnvelope.<ProjectCountResponseDTO>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Ticket count updated successfully")
                        .data(res)
                        .build());
    }

    @DeleteMapping("/{projectId}")
    public Mono<ResponseEnvelope<Void>> deleteCount(@PathVariable UUID projectId) {
        return projectCountService.deleteProjectCount(projectId)
                .thenReturn(ResponseEnvelope.<Void>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Project count deleted successfully")
                        .data(null)
                        .build());
    }
}
