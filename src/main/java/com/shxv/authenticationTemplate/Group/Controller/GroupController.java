package com.shxv.authenticationTemplate.Group.Controller;

import com.shxv.authenticationTemplate.Group.DTO.*;
import com.shxv.authenticationTemplate.Group.Service.GroupMemberService;
import com.shxv.authenticationTemplate.Group.Service.GroupService;
import com.shxv.authenticationTemplate.Util.ResponseEnvelope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Tag(name = "Groups", description = "Group management APIs")
@RestController
@RequestMapping("/group")
public class GroupController {

    @Autowired
    GroupService groupService;

    @Autowired
    GroupMemberService groupMemberService;

    @Operation(
            summary = "Create a new group",
            description = "Creates a group with the authenticated user as leader"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Group created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public Mono<ResponseEnvelope<GroupResponse>> createGroup(@RequestBody GroupCreate groupCreate) {
        return groupService.createGroup(groupCreate)
                .map(createdGroup -> ResponseEnvelope.<GroupResponse>builder()
                        .success(true)
                        .status(201)
                        .message("Group created successfully")
                        .data(createdGroup)
                        .build());
    }

    @Operation(
            summary = "Get group list",
            description = "Fetches the list of all groups available in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Group list fetched successfully"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @GetMapping("/list")
    public Mono<ResponseEnvelope<List<GroupListResponse>>> getGroupList() {
        return groupService.getGroupList()
                .collectList()
                .map(groups -> ResponseEnvelope.<List<GroupListResponse>>builder()
                        .success(true)
                        .status(200)
                        .message("Group list fetched successfully")
                        .data(groups)
                        .build()
                );
    }

    @Operation(
            summary = "Get all groups",
            description = "Returns all groups (admin access)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public Mono<ResponseEnvelope<List<GroupResponse>>> getAllGroups() {
        return groupService.getAllGroups()
                .collectList()
                .map(groups -> ResponseEnvelope.<List<GroupResponse>>builder()
                        .success(true)
                        .status(200)
                        .message("Groups retrieved successfully")
                        .data(groups)
                        .build());
    }

    @Operation(
            summary = "Get my groups",
            description = "Returns groups where the current user is a member or leader"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my")
    public Mono<ResponseEnvelope<List<GroupResponse>>> getMyGroups() {
        return groupService.getMyGroups()
                .collectList()
                .map(groups -> ResponseEnvelope.<List<GroupResponse>>builder()
                        .success(true)
                        .status(200)
                        .message("Groups retrieved successfully")
                        .data(groups)
                        .build());
    }

    @Operation(
            summary = "Get group details",
            description = "Fetch group details by group ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}")
    public Mono<ResponseEnvelope<GroupResponse>> getGroupById(
            @Parameter(description = "Group ID", required = true)
            @PathVariable UUID groupId
    ) {
        return groupService.getGroupById(groupId)
                .map(group -> ResponseEnvelope.<GroupResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Group retrieved successfully")
                        .data(group)
                        .build());
    }

    @Operation(
            summary = "Get group details by ID",
            description = "Fetch detailed information of a group including members and leader"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Group details retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Group not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @GetMapping("/details/{groupId}")
    public Mono<ResponseEnvelope<GroupDetailResponse>> getGroupDetailsById(
            @Parameter(description = "Group ID", required = true)
            @PathVariable UUID groupId
    ) {
        return groupService.getGroupDetailsById(groupId)
                .map(groupDetailResponse -> ResponseEnvelope.<GroupDetailResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Group details retrieved successfully")
                        .data(groupDetailResponse)
                        .build()
                );
    }

    @Operation(
            summary = "Add member to group",
            description = "Leader or admin can add a member to the group"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Member added successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{groupId}/member")
    public Mono<ResponseEnvelope<GroupMemberResponse>> addMember(
            @Parameter(description = "Group ID", required = true)
            @PathVariable UUID groupId,
            @RequestBody GroupMemberCreate groupMemberCreate
    ) {
        return groupMemberService.addMember(groupMemberCreate, groupId)
                .map(addedMember -> ResponseEnvelope.<GroupMemberResponse>builder()
                        .success(true)
                        .status(201)
                        .message("Member added successfully")
                        .data(addedMember)
                        .build());
    }

    @Operation(
            summary = "Update group details",
            description = "Leader or admin can update group name/description"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group updated successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PutMapping("/{groupId}")
    public Mono<ResponseEnvelope<GroupResponse>> updateGroup(
            @PathVariable UUID groupId,
            @RequestBody GroupUpdate groupUpdate
    ) {
        return groupService.updateGroup(groupId, groupUpdate)
                .map(updatedGroup -> ResponseEnvelope.<GroupResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Group updated successfully")
                        .data(updatedGroup)
                        .build());
    }

    @Operation(
            summary = "Transfer group leadership",
            description = "Transfers leadership to an existing group member"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leadership transferred successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid member"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PutMapping("/{groupId}/transfer")
    public Mono<ResponseEnvelope<GroupResponse>> transferLeadership(
            @PathVariable UUID groupId,
            @Parameter(description = "New leader member ID", required = true)
            @RequestParam UUID memberId
    ) {
        return groupService.transferLeadership(groupId, memberId)
                .map(updatedGroup -> ResponseEnvelope.<GroupResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Leadership transferred successfully")
                        .data(updatedGroup)
                        .build());
    }

    @Operation(
            summary = "Remove member from group",
            description = "Leader or admin removes a member from the group"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member removed successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @DeleteMapping("/{groupId}/member/{userId}")
    public Mono<ResponseEnvelope<GroupMemberResponse>> removeMember(
            @PathVariable UUID groupId,
            @PathVariable UUID userId
    ) {
        return groupMemberService.removeMember(groupId, userId)
                .map(removedGroupMember -> ResponseEnvelope.<GroupMemberResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Member removed successfully")
                        .data(removedGroupMember)
                        .build());
    }

    @Operation(
            summary = "Leave group",
            description = "Allows a member to leave the group"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Left group successfully"),
            @ApiResponse(responseCode = "400", description = "Leader cannot leave group")
    })
    @DeleteMapping("/{groupId}/leave")
    public Mono<ResponseEnvelope<GroupResponse>> leaveGroup(@PathVariable UUID groupId) {
        return groupService.leaveGroup(groupId)
                .map(group ->
                        ResponseEnvelope.<GroupResponse>builder()
                                .success(true)
                                .status(200)
                                .message("Left group successfully")
                                .data(group)
                                .build()
                );
    }

    @Operation(
            summary = "Archive group",
            description = "Archives an active group (leader or admin only)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group archived successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PutMapping("/{groupId}/archive")
    public Mono<ResponseEnvelope<GroupResponse>> archiveGroup(@PathVariable UUID groupId) {
        return groupService.archiveGroup(groupId)
                .map(archivedGroup ->
                        ResponseEnvelope.<GroupResponse>builder()
                                .success(true)
                                .status(200)
                                .message("Group archived successfully")
                                .data(archivedGroup)
                                .build()
                );
    }

    @Operation(
            summary = "Restore archived group",
            description = "Restores an archived group (admin only)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group restored successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PutMapping("/{groupId}/restore")
    public Mono<ResponseEnvelope<GroupResponse>> restoreGroup(@PathVariable UUID groupId) {
        return groupService.restoreGroup(groupId)
                .map(restoredGroup ->
                        ResponseEnvelope.<GroupResponse>builder()
                                .success(true)
                                .status(200)
                                .message("Group restored successfully")
                                .data(restoredGroup)
                                .build()
                );
    }

}
