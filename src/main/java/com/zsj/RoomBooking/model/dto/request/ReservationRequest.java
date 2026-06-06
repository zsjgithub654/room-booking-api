package com.zsj.RoomBooking.model.dto.request;

import com.zsj.RoomBooking.validation.TimeRange;

import java.time.LocalDateTime;

@TimeRange(startField = "startTime", endField = "endTime")
public record ReservationRequest(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
}
