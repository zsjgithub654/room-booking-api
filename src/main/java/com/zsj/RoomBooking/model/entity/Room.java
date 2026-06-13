package com.zsj.RoomBooking.model.entity;

import com.zsj.RoomBooking.model.RoomStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

import java.time.LocalTime;

@Entity
public class Room {
    private static final LocalTime OPEN_ALL_DAY_OPEN_TIME = LocalTime.MIN;
    /* Use second precision sentinel instead of LocalTime.MAX because precision loses on persistence. */
    private static final LocalTime OPEN_ALL_DAY_CLOSE_TIME = LocalTime.of(23, 59, 59);

    /* mark the primary key */
    @Id
    /* generate val for id as ID, unique and increment automatically */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String name;
    private Integer capacity;
    private String area;
    private RoomStatus status = RoomStatus.ROOM_STATUS_ACTIVE;
    private LocalTime openTime = OPEN_ALL_DAY_OPEN_TIME;
    private LocalTime closeTime = OPEN_ALL_DAY_CLOSE_TIME;

    public Room(String name, int capacity, String area, LocalTime openTime, LocalTime closeTime) {
        this.name = name;
        this.capacity = capacity;
        this.area = area;
        setOpenHours(openTime, closeTime);
    }

    /* required by JPA */
    public Room() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public String getArea() {
        return area;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return status == RoomStatus.ROOM_STATUS_ACTIVE;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public boolean isOpenAllDay() {
        return OPEN_ALL_DAY_OPEN_TIME.equals(openTime) && OPEN_ALL_DAY_CLOSE_TIME.equals(closeTime);
    }

    /**
     * Set open hours. If both openTime and closeTime are null, the room is open all day.
     * @param openTime: open time of the room
     * @param closeTime: close time of the room
     */
    public void setOpenHours(LocalTime openTime, LocalTime closeTime) {
        if (openTime == null && closeTime == null) {
            this.openTime = OPEN_ALL_DAY_OPEN_TIME;
            this.closeTime = OPEN_ALL_DAY_CLOSE_TIME;
            return;
        }
        if (openTime == null || closeTime == null) {
            throw new IllegalArgumentException("Open time and close time must both be provided or both be omitted.");
        }
        if (!openTime.isBefore(closeTime)) {
            throw new IllegalArgumentException("Open time must be before close time.");
        }
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}

