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
    @Id // mark the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // generate val for id as ID, unique and increment automatically
    private Long id;

    @Version
    private Long version;

    private String name;
    private Integer capacity;
    private String area;
    private RoomStatus status = RoomStatus.ROOM_STATUS_ACTIVE;
    private LocalTime openTime = LocalTime.MIN;
    /* LocalTime.MAX will lose precision in PostgreSQL, use second precision to avoid inconsistency */
    private LocalTime closeTime = LocalTime.of(23, 59, 59);

    public Room(String name, int capacity, String area, LocalTime openTime, LocalTime closeTime) {
        this.name = name;
        this.capacity = capacity;
        this.area = area;
        if (openTime != null) {
            this.openTime = openTime;
        }
        if (closeTime != null) {
            this.closeTime = closeTime;
        }
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

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public LocalTime getOpenTime() {
        return openTime;
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

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }
}

