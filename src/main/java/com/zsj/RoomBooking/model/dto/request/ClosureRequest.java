package com.zsj.RoomBooking.model.dto.request;

import java.time.LocalDateTime;

public record ClosureRequest(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
}