package com.shxv.flowbolt.Dashboard.Service;

import com.shxv.flowbolt.Dashboard.DTO.DashboardResponse;
import reactor.core.publisher.Mono;

public interface DashboardService {
    Mono<DashboardResponse> getDashboardData();
}
