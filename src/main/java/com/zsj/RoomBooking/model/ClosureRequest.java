package com.zsj.RoomBooking.model;

import java.time.LocalDateTime;

public record ClosureRequest(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
}
