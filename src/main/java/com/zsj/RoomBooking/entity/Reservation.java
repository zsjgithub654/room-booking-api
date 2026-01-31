package com.zsj.RoomBooking.entity;

import com.zsj.RoomBooking.ReservationStatus;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private User user;

    @ManyToOne
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
        this.reservationStatus = ReservationStatus.RESERVATION_STATUS_ACTIVE;
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

    public Reservation() {}

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }
}
