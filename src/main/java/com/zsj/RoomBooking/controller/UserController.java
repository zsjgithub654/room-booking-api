package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.mapper.UserMapper;
import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.dto.request.UpdatePasswordRequest;
import com.zsj.RoomBooking.model.dto.request.UpdateUsernameRequest;
import com.zsj.RoomBooking.model.dto.request.UserRequest;
import com.zsj.RoomBooking.model.dto.response.UserResponse;
import com.zsj.RoomBooking.security.CustomUserDetails;
import com.zsj.RoomBooking.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("users")
public class UserController {
    @Autowired
    private UserService service;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable @Positive Long id) {
        return userMapper.toResponse(service.getUser(id));
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return userMapper.toResponse(service.getUser(customUserDetails.getId()));
    }

    @GetMapping
    public List<UserResponse> searchUser(@RequestParam String username, @RequestParam Role role,
                                         @RequestParam UserStatus status) {
        return service.searchUsers(username, role, status).stream().map(userMapper::toResponse).toList();
    public List<UserResponse> searchUser(@Valid @ModelAttribute SearchUserRequest request) {
        return service.searchUsers(request.username(), request.role(), request.status()).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<UserResponse> addUser(@Valid @RequestBody UserRequest userRequest) {
        return new ResponseEntity<>(
                userMapper.toResponse(service.addUser(userMapper.toEntity(userRequest))),
                HttpStatus.CREATED
        );
    }

    @PatchMapping("/{id}/username")
    public UserResponse updateUsername(@PathVariable @Positive Long id, @Valid @RequestBody UpdateUsernameRequest request) {
        return userMapper.toResponse(service.updateUsername(id, request.username()));
    }

    @PatchMapping("/me/username")
    public UserResponse updateCurrentUsername(@AuthenticationPrincipal CustomUserDetails customUserDetails, @Valid @RequestBody UpdateUsernameRequest request) {
        return userMapper.toResponse(service.updateUsername(customUserDetails.getId(), request.username()));
    }

    @PatchMapping("/{id}/password")
    public UserResponse updatePassword(@PathVariable @Positive Long id, @Valid @RequestBody UpdatePasswordRequest request) {
        return userMapper.toResponse(service.updatePassword(id, request.password()));
    }

    @PatchMapping("/me/password")
    public UserResponse updateCurrentPassword(@AuthenticationPrincipal CustomUserDetails customUserDetails, @Valid @RequestBody UpdatePasswordRequest request) {
        return userMapper.toResponse(service.updatePassword(customUserDetails.getId(), request.password()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponse> closeUserAccount(@PathVariable @Positive Long id) {
        service.closeUserAccount(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<UserResponse> closeCurrentUserAccount(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        service.closeUserAccount(customUserDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
