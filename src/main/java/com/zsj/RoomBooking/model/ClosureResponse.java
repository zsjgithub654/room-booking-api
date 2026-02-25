package com.zsj.RoomBooking.model;

import java.time.LocalDateTime;

public record ClosureResponse(Long id, Long userId, Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
}
