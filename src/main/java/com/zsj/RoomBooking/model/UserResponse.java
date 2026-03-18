package com.zsj.RoomBooking.model;

import java.util.Set;

public record UserResponse(Long id, String username, Set<Role> roles) {
}