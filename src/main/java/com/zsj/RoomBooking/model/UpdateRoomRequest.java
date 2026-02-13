package com.zsj.RoomBooking.model;

import java.time.LocalDateTime;

public class UpdateRoomRequest {
    private final String name;
    private final int capacity;
    private final String area;

    public UpdateRoomRequest(String name, int capacity, String area, LocalDateTime unavailableStartTime, LocalDateTime unavailableEndTime) {
        this.name = name;
        this.capacity = capacity;
        this.area = area;
    }
}
