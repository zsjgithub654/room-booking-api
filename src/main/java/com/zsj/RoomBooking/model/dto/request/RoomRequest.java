package com.zsj.RoomBooking.model.dto.request;

import com.zsj.RoomBooking.validation.TimeInterval;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

@TimeInterval(
        fromField = "openTime",
        toField = "closeTime",
        message = "open time must be before close time."
)
/* TODO: maybe add blank lines between params */
public record RoomRequest(
        @NotBlank
        @Size(max = 20)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "name can only contain letters, numbers, dots, underscores, and hyphens.")
        String name,
        @Positive Integer capacity,
        @Size(max = 20)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "name can only contain letters, numbers, dots, underscores, and hyphens.")
        String area,
        LocalTime openTime,
        LocalTime closeTime) {
}
