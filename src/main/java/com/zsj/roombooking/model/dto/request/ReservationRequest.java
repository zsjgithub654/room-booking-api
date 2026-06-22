package com.zsj.roombooking.model.dto.request;

import com.zsj.roombooking.validation.MinutePrecision;
import com.zsj.roombooking.validation.TimeInterval;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@TimeInterval(startField = "startTime", endField = "endTime")
public record ReservationRequest(@NotNull @Positive Long roomId,
                                 @NotNull @Future @MinutePrecision LocalDateTime startTime,
                                 @NotNull @Future @MinutePrecision LocalDateTime endTime) {
}
