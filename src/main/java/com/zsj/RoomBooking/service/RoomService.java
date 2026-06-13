package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.criteria.RoomSearchCriteria;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.result.RoomSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface RoomService {
    Page<Room> searchRooms(RoomSearchCriteria criteria, Pageable pageable);
    List<RoomSchedule> searchAvailabilities(String name, Integer minCapacity, Integer maxCapacity, String area,
                                            LocalDate startDate, LocalDate endDate, Boolean includeUnavailable);
    Room getRoom(Long id);
    Room addRoom(Room room);
    List<Reservation> deleteRoom(Long id);
    Room updateRoom(Long id, String name, Integer capacity, String area, LocalTime openTime, LocalTime closeTime);
}