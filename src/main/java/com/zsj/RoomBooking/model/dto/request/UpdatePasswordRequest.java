package com.zsj.RoomBooking.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdatePasswordRequest(@NotBlank String password) {
}
