package com.zsj.RoomBooking.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class SearchRoomRequest {
    private final String name;
    private final int capacity;
    private final String area;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public SearchRoomRequest(String name, int capacity, String area, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.name = name;
        this.capacity = capacity;
        this.area = area;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
