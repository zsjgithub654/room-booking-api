package com.zsj.RoomBooking.model.dto.request;

import com.zsj.RoomBooking.validation.TimeRange;
import jakarta.validation.constraints.Future;

import java.time.LocalDateTime;

@TimeRange(startField = "startTime", endField = "endTime")
public record UpdateReservationRequest(@Future LocalDateTime startTime, @Future LocalDateTime endTime) {
}
