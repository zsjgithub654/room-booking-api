package com.zsj.RoomBooking.model.dto.request;

import com.zsj.RoomBooking.validation.Range;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Range(fromField = "startTime", toField = "endTime", message = "start time must be before end time.")
public record UpdateReservationRequest(@NotNull @Future LocalDateTime startTime,
                                       @NotNull @Future LocalDateTime endTime) {
}
