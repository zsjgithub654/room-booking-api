package com.zsj.RoomBooking.service.impl;

import com.zsj.RoomBooking.model.Availability;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.dto.response.RoomResponse;
import com.zsj.RoomBooking.repository.RoomRepository;
import com.zsj.RoomBooking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {
    @Autowired
    private RoomRepository roomRepository;

    @Override
    public List<Room> searchRooms(String name, Integer minCapacity, Integer maxCapacity, String area) {
        return null;
    }

    @Override
    public List<Availability> searchAvailabilities(String name, Integer minCapacity, Integer maxCapacity, String area, LocalDateTime startTime, LocalDateTime endTime) {
        return null;
    }

    @Override
    public Room getRoom(Long id) {
        return roomRepository.getReferenceById(id);
    }

    @Override
    public Room addRoom(Room room) {
        return roomRepository.save(new Room(room.getName(), room.getCapacity(), room.getArea()));
    }

    @Override
    public List<Reservation> deleteRoom(Long id) {
        return null;
    }

    @Override
    public Room updateRoom(Long id, String name, Integer capacity, String area) {
        return null;
    }

    /**
     * generate a dto object from a Room object to return
     * @param room: Room object
     * @return RoomResponse object
     */
    private RoomResponse getRoomResponse(Room room) {
        return new RoomResponse(room.getId(), room.getName(), room.getCapacity(), room.getArea());
    }
}