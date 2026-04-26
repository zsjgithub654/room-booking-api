package com.zsj.RoomBooking.model.dto.request;

import java.time.LocalDateTime;

public record ReservationRequest(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
}
