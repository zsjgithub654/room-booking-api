package com.zsj.RoomBooking.model.dto.request;

import com.zsj.RoomBooking.validation.TimeRange;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@TimeRange(startField = "startTime", endField = "endTime")
public record ReservationRequest(@NotNull Long roomId,
                                 @NotNull @Future LocalDateTime startTime,
                                 @NotNull @Future LocalDateTime endTime) {
}
