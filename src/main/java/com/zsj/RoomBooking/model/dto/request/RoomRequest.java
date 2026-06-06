package com.zsj.RoomBooking.model.dto.request;

import com.zsj.RoomBooking.validation.TimeRange;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalTime;

@TimeRange(
        startField = "openTime",
        endField = "closeTime",
        message = "open time must be before close time."
)
public record RoomRequest(@NotBlank String name, Integer capacity, String area, LocalTime openTime, LocalTime closeTime) {
}