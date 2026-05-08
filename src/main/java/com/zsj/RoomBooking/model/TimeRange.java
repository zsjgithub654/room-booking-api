package com.zsj.RoomBooking.model;

import java.time.LocalDateTime;

public class TimeRange {
    LocalDateTime startTime;
    LocalDateTime endTime;

    public TimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        /* TODO: validate */
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}