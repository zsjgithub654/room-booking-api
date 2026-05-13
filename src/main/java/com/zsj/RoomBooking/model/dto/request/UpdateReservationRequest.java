package com.zsj.RoomBooking.model.dto.request;

import java.time.LocalDateTime;

public record UpdateReservationRequest(LocalDateTime startTime, LocalDateTime endTime) {
}
