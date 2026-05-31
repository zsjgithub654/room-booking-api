package com.zsj.RoomBooking.model.entity;

import com.zsj.RoomBooking.model.RoomStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Id;

import java.time.LocalTime;
import java.util.Set;

@Entity
public class Room {
    @Id // mark the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // generate val for id as ID, unique and increment automatically
    private Long id;
    private String name;
    private Integer capacity;
    private String area;
    private RoomStatus status;
    private LocalTime openTime = LocalTime.MIN;
    private LocalTime closeTime = LocalTime.MAX;

    @OneToMany(mappedBy = "room")
    private Set<Reservation> reservations;

    /* TODO: need this or not? */
    @OneToMany(mappedBy = "room")
    private Set<Closure> closures;

    public Room(String name, int capacity, String area, LocalTime openTime, LocalTime closeTime) {
        this.name = name;
        this.capacity = capacity;
        this.area = area;
        this.status = RoomStatus.ROOM_STATUS_ACTIVE;
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

