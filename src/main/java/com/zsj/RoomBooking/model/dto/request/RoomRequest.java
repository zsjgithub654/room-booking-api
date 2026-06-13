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
public record RoomRequest(
        @NotBlank
        @Size(max = 50)
        @Pattern(regexp = "^[A-Za-z0-9._ -]+$", message = "name can only contain letters, numbers, spaces, dots, underscores, and hyphens.")
        String name,
        @Positive
        Integer capacity,
        @Size(max = 50)
        @Pattern(regexp = "^(?=.*\\S)[A-Za-z0-9._ -]+$", message = "area can only contain letters, numbers, spaces, dots, underscores, and hyphens.")
        String area,
        LocalTime openTime,
        LocalTime closeTime) {
}
