package com.shxv.authenticationTemplate.Ticket.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StatusUpdate {

    UUID ticketId;
    String status;

}
