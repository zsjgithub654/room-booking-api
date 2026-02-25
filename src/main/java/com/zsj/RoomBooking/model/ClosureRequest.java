package com.zsj.RoomBooking.model;

import java.time.LocalDateTime;

public record ClosureRequest(Long userId, Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
}
