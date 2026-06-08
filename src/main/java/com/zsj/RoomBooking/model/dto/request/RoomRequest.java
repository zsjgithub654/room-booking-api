package com.zsj.RoomBooking.model.dto.request;

import com.zsj.RoomBooking.validation.TimeInterval;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.time.LocalTime;

@TimeInterval(
        fromField = "openTime",
        toField = "closeTime",
        message = "open time must be before close time."
)
public record RoomRequest(@NotBlank String name, @Positive Integer capacity, String area, LocalTime openTime, LocalTime closeTime) {
}
