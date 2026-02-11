package com.zsj.RoomBooking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Id;

import java.util.Set;

@Entity
public class Room {
    @Id // mark the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // generate val for id as ID, unique and increment automatically
    /* TODO: y don't use primitive */
    private Long id;
    private String name;
    private Integer capacity;
    // TODO: consider building a table for area
    private String area;

    @OneToMany(mappedBy = "room")
    private Set<Reservation> reservations;

    public Room(String name, int capacity, String area) {
        this.name = name;
        this.capacity = capacity;
        this.area = area;
    }
    // JPA requires default constructor to instantiate
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
}

