package com.zsj.RoomBooking.model.dto.request;

import com.zsj.RoomBooking.validation.MinutePrecision;
import com.zsj.RoomBooking.validation.TimeInterval;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@TimeInterval(fromField = "startTime", toField = "endTime")
public record ReservationRequest(@NotNull @Positive Long roomId,
                                 @NotNull @Future @MinutePrecision LocalDateTime startTime,
                                 @NotNull @Future @MinutePrecision LocalDateTime endTime) {
}
