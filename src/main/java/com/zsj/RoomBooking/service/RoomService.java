package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.Availability;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomService {
    List<Room> searchRooms(String name, Integer minCapacity, Integer maxCapacity, String area);
    List<Availability> searchAvailabilities(String name, Integer minCapacity, Integer maxCapacity, String area, LocalDateTime startTime, LocalDateTime endTime);
    Room getRoom(Long id);
    Room addRoom(Room room);
    List<Reservation> deleteRoom(Long id);
    Room updateRoom(Long id, String name, Integer capacity, String area);
}