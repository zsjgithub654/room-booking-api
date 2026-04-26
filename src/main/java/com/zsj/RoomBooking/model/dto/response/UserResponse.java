package com.zsj.RoomBooking.model.dto.response;

import com.zsj.RoomBooking.model.Role;

import java.util.Set;

public record UserResponse(Long id, String username, Set<Role> roles) {
}