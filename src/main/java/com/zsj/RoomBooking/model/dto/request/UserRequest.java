package com.zsj.RoomBooking.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank
        @Size(min = 3, max = 20)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "username can only contain letters, numbers, dots, underscores, and hyphens.")
        String username,
        @NotBlank
        @Size(min = 8, max = 30)
        @Pattern(regexp = "^[A-Za-z0-9\\p{Punct}]+$", message = "password can only contain letters, numbers, and common special characters.")
        String password) {
}
