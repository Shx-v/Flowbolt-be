package com.shxv.authenticationTemplate.Dashboard.Controller;

import com.shxv.authenticationTemplate.Dashboard.DTO.DashboardResponse;
import com.shxv.authenticationTemplate.Dashboard.Service.DashboardService;
import com.shxv.authenticationTemplate.Util.ResponseEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    DashboardService dashboardService;

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
