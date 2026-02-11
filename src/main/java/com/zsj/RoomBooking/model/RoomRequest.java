package com.zsj.RoomBooking.model;

public class RoomRequest {
    private final String name;
    private final int capacity;
    private final String area;

    public RoomRequest(String name, int capacity, String area) {
        this.name = name;
        this.capacity = capacity;
        this.area = area;
    }
    /* required by jackson */
    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getArea() {
        return area;
    }
}
