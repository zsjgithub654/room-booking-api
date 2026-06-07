package com.zsj.RoomBooking.model.dto.request;

import com.zsj.RoomBooking.validation.MinutePrecision;
import com.zsj.RoomBooking.validation.Interval;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Interval(fromField = "startTime", toField = "endTime", message = "start time must be before end time.")
public record ClosureRequest(@NotNull @Positive Long roomId,
                             @NotNull @Future @MinutePrecision LocalDateTime startTime,
                             @NotNull @Future @MinutePrecision LocalDateTime endTime) {
}
