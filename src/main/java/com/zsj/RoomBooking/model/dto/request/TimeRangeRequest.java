package com.zsj.RoomBooking.model.dto.request;

import java.time.LocalDateTime;

public record TimeRangeRequest(LocalDateTime startTime, LocalDateTime endTime) {
}
