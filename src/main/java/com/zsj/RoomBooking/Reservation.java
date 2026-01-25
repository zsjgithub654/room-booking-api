package com.zsj.RoomBooking;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.DateTimeException;
import java.time.LocalDateTime;

public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private User user;
    private Room room;
    // date and time without time zone, weekday?
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus reservationStatus;

    public Reservation(User user, Room room, LocalDateTime startTime, LocalDateTime endTime) {
        this.user = user;
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * modify reservation time
     * @param startTime new start time.
     * @param endTime new end time.
     * @return if successfully modified.
     */
    public boolean modifyTime(LocalDateTime startTime, LocalDateTime endTime) {
        return true;
    }

    /**
     * cancel reservation
     * @return if successfully canceled
     */
    public boolean cancel() {
        return true;
    }
}
