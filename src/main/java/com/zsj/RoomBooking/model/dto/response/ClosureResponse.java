package com.zsj.RoomBooking.model.dto.response;

import java.time.LocalDateTime;

public record ClosureResponse(Long id, Long userId, Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
}