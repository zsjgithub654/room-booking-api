package com.zsj.RoomBooking.model.entity;

import com.zsj.RoomBooking.model.Occupation;
import com.zsj.RoomBooking.model.OccupationType;
import com.zsj.RoomBooking.model.ReservationStatus;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_reservation_room_status_start_time", columnList = "room_id, status, start_time"),
        @Index(name = "idx_reservation_user_status_start_time", columnList = "user_id, status, start_time")
})
public class Reservation implements Occupation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne
    private User user;

    @ManyToOne
    private Room room;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;

    public Reservation(User user, Room room, LocalDateTime startTime, LocalDateTime endTime) {
        this.user = user;
        this.room = room;
        setTime(startTime, endTime);
        this.status = ReservationStatus.RESERVATION_STATUS_ACTIVE;
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

    /* required by JPA */
    public Reservation() {}

    @Override
    public OccupationType getOccupationType() {
        return OccupationType.OCCUPATION_TYPE_RESERVATION;
    }

    public Long getId() {
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

    public void setTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time must not be null.");
        }
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return status == ReservationStatus.RESERVATION_STATUS_ACTIVE;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
