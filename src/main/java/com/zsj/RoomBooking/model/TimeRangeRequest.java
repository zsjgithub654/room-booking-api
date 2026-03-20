package com.zsj.RoomBooking.model;

import java.time.LocalDateTime;

public record TimeRangeRequest(LocalDateTime startTime, LocalDateTime endTime) {
}
