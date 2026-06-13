package com.zsj.RoomBooking.model.result;

import com.zsj.RoomBooking.model.Occupation;
import com.zsj.RoomBooking.model.entity.Room;

import java.util.List;

public record RoomSchedule(Room room, List<Occupation> occupations) {
}