package com.zsj.RoomBooking.model.dto.request;

import com.zsj.RoomBooking.validation.MinutePrecision;
import com.zsj.RoomBooking.validation.TimeInterval;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@TimeInterval(startField = "startTime", endField = "endTime")
public record ClosureRequest(@NotNull @Future @MinutePrecision LocalDateTime startTime,
                             @NotNull @Future @MinutePrecision LocalDateTime endTime) {
}
