package com.zsj.RoomBooking.service.impl;

import com.zsj.RoomBooking.entity.Room;
import com.zsj.RoomBooking.model.RoomRequest;
import com.zsj.RoomBooking.model.RoomResponse;
import com.zsj.RoomBooking.repository.RoomRepository;
import com.zsj.RoomBooking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {
    @Autowired
    private RoomRepository roomRepository;

    @Override
    public List<RoomResponse> getAllRooms() {
        List<Room> rooms =  roomRepository.findAll();
        List<RoomResponse> roomResponses = new ArrayList<>();
        for (Room room : rooms) {
            roomResponses.add(getRoomResponse(room));
        }
        return roomResponses;
    }

    @Override
    public RoomResponse addRoom(RoomRequest roomRequest) {
        Room room = roomRepository.save(
                new Room(roomRequest.getName(), roomRequest.getCapacity(), roomRequest.getArea()));
        return getRoomResponse(room);
    }

    @Override
    public RoomResponse deleteRoom(Long id) {
        return null;
    }

    private RoomResponse getRoomResponse(Room room) {
        return new RoomResponse(room.getId(), room.getName(), room.getCapacity(), room.getArea());
    }
}
