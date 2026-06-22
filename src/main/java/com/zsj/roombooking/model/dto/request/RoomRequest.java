package com.zsj.roombooking.model.dto.request;

import com.zsj.roombooking.validation.TimeInterval;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

@TimeInterval(
        startField = "openTime",
        endField = "closeTime",
        message = "{room.open-hours.time-interval}"
)
public record RoomRequest(
        @NotBlank
        @Size(max = 50)
        @Pattern(regexp = "^[A-Za-z0-9._ -]+$", message = "{room.name.pattern}")
        String name,
        @Positive
        Integer capacity,
        @Size(max = 50)
        @Pattern(regexp = "^(?=.*\\S)[A-Za-z0-9._ -]+$", message = "{room.area.pattern}")
        String area,
        LocalTime openTime,
        LocalTime closeTime) {
}
