package com.zsj.RoomBooking.model.dto.request;

import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.UserStatus;
import jakarta.validation.constraints.Size;

public record SearchUserRequest(
        @Size(max = 20)
        String username,
        Role role,
        UserStatus status
) {
}
