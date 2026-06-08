package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.RoomSchedule;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface RoomService {
    List<Room> searchRooms(String name, Integer minCapacity, Integer maxCapacity, String area);
    List<RoomSchedule> searchAvailabilities(String name, Integer minCapacity, Integer maxCapacity, String area,
                                            LocalDate startDate, LocalDate endDate, Boolean includeUnavailable);
    Room getRoom(Long id);
    Room addRoom(Room room);
    List<Reservation> deleteRoom(Long id);
    Room updateRoom(Long id, String name, Integer capacity, String area, LocalTime openTime, LocalTime closeTime);
}