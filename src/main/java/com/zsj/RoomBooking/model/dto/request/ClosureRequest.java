package com.zsj.RoomBooking.model.dto.request;

import com.zsj.RoomBooking.validation.MinutePrecision;
import com.zsj.RoomBooking.validation.TimeInterval;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@TimeInterval(fromField = "startTime", toField = "endTime", message = "start time must be before end time.")
public record ClosureRequest(@NotNull @Future @MinutePrecision LocalDateTime startTime,
                             @NotNull @Future @MinutePrecision LocalDateTime endTime) {
}
