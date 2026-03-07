package com.shxv.flowbolt.Dashboard.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class TicketTrend {
    private List<DailyStat> last7DaysCreated;
    private List<DailyStat> last7DaysResolved;
}
