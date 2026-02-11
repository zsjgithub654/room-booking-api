package com.zsj.RoomBooking.model;

public class RoomResponse {
    private final Long id;
    private final String name;
    private final Integer capacity;
    private final String area;

    public RoomResponse(Long id, String name, Integer capacity, String area) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.area = area;
    }

    /* required by jackson */
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
