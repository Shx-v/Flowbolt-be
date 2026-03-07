package com.shxv.flowbolt.Auth.Service;

import com.shxv.flowbolt.Auth.DTO.UserDetails;
import com.shxv.flowbolt.Auth.DTO.UserListResponse;
import com.shxv.flowbolt.Auth.DTO.UserResponse;
import com.shxv.flowbolt.Auth.Model.User;
import com.shxv.flowbolt.Auth.Repository.UserRepository;
import com.shxv.flowbolt.Auth.Util.UserRoleUtil;
import com.shxv.flowbolt.Role.Service.GlobalPermissionService;
import com.shxv.flowbolt.Role.Service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleService roleService;

    @Autowired
    GlobalPermissionService globalPermissionService;

    @Autowired
    UserRoleUtil userRoleUtil;

    @Override
    public Flux<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .flatMap(this::buildUserResponse);
    }

    @Override
    public Mono<UserResponse> getUserById(UUID uuid) {
        return userRepository.findById(uuid)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .flatMap(this::buildUserResponse);
    }

    @Override
    public Mono<UserDetails> getUserDetailsById(UUID uuid) {
        return userRepository.findById(uuid)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .flatMap(this::buildUserDetails);
    }

    @Override
    public Mono<UserDetails> getCurrentUserDetails() {
        return userRoleUtil.getUserId()
                .flatMap(this::getUserDetailsById);
    }

    @Override
    public Flux<UserListResponse> getUserList() {
        return userRepository.findAll()
                .map(user -> UserListResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .build());
    }

    //HELPER METHODS
    private Mono<UserDetails> buildUserDetails(User user) {

        return Mono.zip(
                roleService.getRoleById(user.getRole()),
                globalPermissionService.getAllPermissions(user.getRole()).collectList()
        ).map(tuple -> {

            var role = tuple.getT1();
            var permissions = tuple.getT2();

            return UserDetails.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(role)
                    .permissions(permissions)
                    .logoPath(user.getLogoPath())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
        });
    }

    private Mono<UserResponse> buildUserResponse(User user) {
        return roleService.getRoleById(user.getRole())
                .map(roleResponse ->
                        UserResponse.builder()
                                .id(user.getId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .phoneNumber(user.getPhoneNumber())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .role(roleResponse.getId())
                                .roleName(roleResponse.getName())
                                .logoPath(user.getLogoPath())
                                .createdAt(user.getCreatedAt())
                                .updatedAt(user.getUpdatedAt())
                                .build()
                );
    }
}
