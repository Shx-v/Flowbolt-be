package com.shxv.authenticationTemplate.Group.Repository;

import com.shxv.authenticationTemplate.Group.Enum.GroupStatus;
import com.shxv.authenticationTemplate.Group.Model.Group;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface GroupRepository extends ReactiveCrudRepository<Group, UUID> {

    Flux<Group> findAllByStatus(GroupStatus status);

    @Query("""
                SELECT DISTINCT g.*
                FROM groups g
                LEFT JOIN group_members gm ON g.id = gm.group
                WHERE (g.leader = :userId OR gm.member = :userId)
                  AND g.status = 'ACTIVE'
            """)
    Flux<Group> findMyGroups(UUID userId);

}
