package com.zsj.roombooking.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUsernameRequest(
        @NotBlank
        @Size(min = 3, max = 20)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "{user.username.pattern}")
        String username) {
}
