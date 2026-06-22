package com.zsj.roombooking.model.result;

import com.zsj.roombooking.model.Occupation;
import com.zsj.roombooking.model.entity.Room;

import java.util.List;

public record RoomSchedule(Room room,
                           List<Occupation> occupations) {
}