package com.zsj.RoomBooking;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Room {
    @Id // mark the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // generate val for id as ID, unique and increment automatically
    private Long id;
    private String displayName;
    private Integer capacity;
    private String area; // String or Enum?

    public Room(String displayName, int capacity, String area) {
        this.displayName = displayName;
        this.capacity = capacity;
        this.area = area;
    }
    // JPA requires default constructor to instantiate
    public Room() {
    }
    // Jackson requires getters to parse json
    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public String getArea() {
        return area;
    }
}

