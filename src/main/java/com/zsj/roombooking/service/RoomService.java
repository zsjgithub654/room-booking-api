package com.zsj.roombooking.service;

import com.zsj.roombooking.model.criteria.RoomSearchCriteria;
import com.zsj.roombooking.model.entity.Reservation;
import com.zsj.roombooking.model.entity.Room;
import com.zsj.roombooking.model.result.RoomSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface RoomService {
    Page<Room> searchRooms(RoomSearchCriteria criteria, Pageable pageable);
    List<RoomSchedule> searchRoomSchedules(String name, Integer minCapacity, Integer maxCapacity, String area,
                                           LocalDate fromDate, LocalDate toDate, Boolean includeUnavailable);
    Room getRoom(Long id);
    Room addRoom(Room room);
    List<Reservation> deleteRoom(Long id);
    Room updateRoom(Long id, String name, Integer capacity, String area, LocalTime openTime, LocalTime closeTime);
}