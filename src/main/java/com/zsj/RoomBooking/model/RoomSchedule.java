package com.zsj.RoomBooking.model;

import com.zsj.RoomBooking.model.entity.Room;

import java.util.List;

public record RoomSchedule(Room room, List<Occupation> occupations) {

}