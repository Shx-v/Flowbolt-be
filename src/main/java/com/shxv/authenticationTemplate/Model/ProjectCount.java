package com.shxv.authenticationTemplate.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("project_count")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectCount {

    @Id
    @Column("id")
    private UUID id;

    @Column("project")
    private UUID project;

    @Column("ticket_count")
    private Integer ticketCount;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
