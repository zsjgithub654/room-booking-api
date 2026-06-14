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
    private static final String START_AND_END_TIME_MUST_NOT_BE_NULL = "Start time and end time must not be null.";
    private static final String START_TIME_MUST_BE_BEFORE_END_TIME = "Start time must be before end time.";

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
        this.status = ReservationStatus.RESERVATION_STATUS_SCHEDULED;
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
            throw new IllegalArgumentException(START_AND_END_TIME_MUST_NOT_BE_NULL);
        }
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(START_TIME_MUST_BE_BEFORE_END_TIME);
        }
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public boolean isScheduled() {
        return status == ReservationStatus.RESERVATION_STATUS_SCHEDULED;
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
