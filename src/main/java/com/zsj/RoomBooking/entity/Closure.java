package com.zsj.RoomBooking.entity;

import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.RoleType;
import com.zsj.RoomBooking.model.TimeRange;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

public class Closure implements TimeRange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Room room;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Override
    public LocalDateTime getStartTime() {
        return this.startTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return this.endTime;
    }
}