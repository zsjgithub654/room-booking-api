package com.zsj.RoomBooking.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUsernameRequest(@NotBlank String username) {
}
