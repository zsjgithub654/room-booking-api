package com.zsj.roombooking.model.dto.response;

import com.zsj.roombooking.model.Role;
import com.zsj.roombooking.model.UserStatus;

import java.util.Set;

public record UserResponse(Long id,
                           String username,
                           Set<Role> roles,
                           UserStatus status) {
}