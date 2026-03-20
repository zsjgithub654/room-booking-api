package com.zsj.RoomBooking.service.impl;

import com.zsj.RoomBooking.entity.Room;
import com.zsj.RoomBooking.model.AddClosureResponse;
import com.zsj.RoomBooking.model.ClosureRequest;
import com.zsj.RoomBooking.model.ClosureResponse;
import com.zsj.RoomBooking.model.RoomRequest;
import com.zsj.RoomBooking.model.RoomResponse;
import com.zsj.RoomBooking.model.SearchRoomRequest;
import com.zsj.RoomBooking.repository.RoomRepository;
import com.zsj.RoomBooking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {
    @Autowired
    private RoomRepository roomRepository;

    @Override
    public List<RoomResponse> searchRooms(SearchRoomRequest searchRoomRequest) {
        return null;
    }

    @Override
    public RoomResponse getRoom(Long id) {
        return getRoomResponse(roomRepository.getReferenceById(id));
    }

    @Override
    public RoomResponse addRoom(RoomRequest roomRequest) {
        Room room = roomRepository.save(
                new Room(roomRequest.name(), roomRequest.capacity(), roomRequest.area()));
        return getRoomResponse(room);
    }

    @Override
    public RoomResponse deleteRoom(Long id) {
        return null;
    }

    @Override
    public RoomResponse updateRoom(Long id, RoomRequest roomRequest) {
        return null;
    }

    @Override
    public List<ClosureResponse> getClosures(Long roomId) {
        return null;
    }

    @Override
    public AddClosureResponse addClosure(Long roomId, ClosureRequest closureRequest) {
        return null;
    }

    @Override
    public ClosureResponse deleteClosure(Long roomId, Long closureId) {
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