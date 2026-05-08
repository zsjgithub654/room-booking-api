package com.zsj.RoomBooking.model;

import com.zsj.RoomBooking.model.entity.Room;

import java.util.List;

public class Availability {
    Room room;
    List<TimeRange> availableSlots;

    public Room getRoom() {
        return room;
    }

    public List<TimeRange> getAvailableSlots() {
        return availableSlots;
    }

    public Availability(Room room, List<TimeRange> availableSlots) {
        this.room = room;
        this.availableSlots = availableSlots;
    }
}