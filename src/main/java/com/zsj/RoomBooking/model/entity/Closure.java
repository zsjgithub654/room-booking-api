package com.zsj.RoomBooking.model.entity;

import com.zsj.RoomBooking.model.Occupation;
import com.zsj.RoomBooking.model.OccupationType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_closure_room_start_time", columnList = "room_id, start_time")
})
public class Closure implements Occupation {
    private static final String START_AND_END_TIME_MUST_NOT_BE_NULL = "Start time and end time must not be null.";
    private static final String START_TIME_MUST_BE_BEFORE_END_TIME = "Start time must be before end time.";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne
    private Room room;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Closure(Room room, LocalDateTime startTime, LocalDateTime endTime) {
        this.room = room;
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException(START_AND_END_TIME_MUST_NOT_BE_NULL);
        }
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(START_TIME_MUST_BE_BEFORE_END_TIME);
        }
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Closure() {
    }

    @Override
    public OccupationType getOccupationType() {
        return OccupationType.OCCUPATION_TYPE_CLOSURE;
    }

    public Long getId() {
        return id;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDateTime getStartTime() {
        return this.startTime;
    }

    public LocalDateTime getEndTime() {
        return this.endTime;
    }
}
