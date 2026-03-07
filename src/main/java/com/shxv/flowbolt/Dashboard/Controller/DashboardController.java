package com.shxv.flowbolt.Dashboard.Controller;

import com.shxv.flowbolt.Dashboard.DTO.DashboardResponse;
import com.shxv.flowbolt.Dashboard.Service.DashboardService;
import com.shxv.flowbolt.Util.ResponseEnvelope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Dashboard related APIs")
public class DashboardController {

    @Autowired
    DashboardService dashboardService;

    @Operation(
            summary = "Get dashboard data",
            description = "Fetches aggregated dashboard metrics for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    public Mono<ResponseEnvelope<DashboardResponse>> getDashboardData() {
        return dashboardService.getDashboardData()
                .map(dashboardResponse -> ResponseEnvelope.<DashboardResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Dashboard data retrieved successfully")
                        .data(dashboardResponse)
                        .build()
                );
    }

}
