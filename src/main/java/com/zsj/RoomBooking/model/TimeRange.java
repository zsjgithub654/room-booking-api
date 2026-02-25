package com.zsj.RoomBooking.model;

import java.time.LocalDateTime;

public interface TimeRange {
    LocalDateTime getStartTime();
    LocalDateTime getEndTime();
}