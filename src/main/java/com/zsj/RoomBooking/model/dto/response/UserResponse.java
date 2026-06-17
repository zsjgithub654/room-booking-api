package com.zsj.RoomBooking.model.dto.response;

import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.UserStatus;

import java.util.Set;

public record UserResponse(Long id,
                           String username,
                           Set<Role> roles,
                           UserStatus status) {
}