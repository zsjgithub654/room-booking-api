package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.entity.Room;
import com.zsj.RoomBooking.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomServiceImpl implements RoomService{
    @Autowired
    private RoomRepository roomRepository;

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public Room addRoom(String name, int capacity, String area) {
        return roomRepository.save(new Room(name, capacity, area));
    }
}
