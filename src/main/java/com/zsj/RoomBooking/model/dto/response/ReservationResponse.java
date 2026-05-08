package com.zsj.RoomBooking.model.dto.response;

import com.zsj.RoomBooking.model.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationResponse(Long id, Long userId, Long roomId, LocalDateTime startTime, LocalDateTime endTime,
                                  ReservationStatus status) {
}
