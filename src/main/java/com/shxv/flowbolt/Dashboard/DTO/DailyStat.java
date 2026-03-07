package com.shxv.flowbolt.Dashboard.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Builder
@Getter
@Setter
public class DailyStat {
    private LocalDate date;
    private Long count;
}
