package com.zsj.RoomBooking.model;

import java.time.LocalDateTime;

public record ReservationRequest(Long userId, Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
}
