package com.shxv.authenticationTemplate.DTO;

import com.shxv.authenticationTemplate.Enum.TicketPriorityEnum;
import com.shxv.authenticationTemplate.Enum.TicketStatusEnum;
import com.shxv.authenticationTemplate.Enum.TicketTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketUpdateRequestDTO {

    private String title;
    private String description;
    private String priority;
    private String type;
}
