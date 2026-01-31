package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.entity.Room;

import java.util.List;

public interface RoomService {
    public List<Room> getAllRooms();
    public Room addRoom(String name, int capacity, String area);
}
