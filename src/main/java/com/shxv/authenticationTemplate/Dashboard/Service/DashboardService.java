package com.shxv.authenticationTemplate.Dashboard.Service;

import com.shxv.authenticationTemplate.Dashboard.DTO.DashboardResponse;
import reactor.core.publisher.Mono;

public interface DashboardService {
    Mono<DashboardResponse> getDashboardData();
}
