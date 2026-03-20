package com.zsj.RoomBooking.model;

import java.time.LocalDateTime;

public record ReservationRequest(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
}
