package com.zsj.RoomBooking.model;

import com.zsj.RoomBooking.model.entity.Room;

import java.util.List;

public class RoomSchedule {
    Room room;
    List<Occupation> occupations;

    public Room getRoom() {
        return room;
    }

    public List<Occupation> getOccupations() {
        return occupations;
    }

    public RoomSchedule(Room room, List<Occupation> occupations) {
        this.room = room;
        this.occupations = occupations;
    }
}