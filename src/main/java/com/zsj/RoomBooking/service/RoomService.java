package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.RoomRequest;
import com.zsj.RoomBooking.model.RoomResponse;

import java.util.List;

public interface RoomService {
    List<RoomResponse> getAllRooms();
    RoomResponse addRoom(RoomRequest roomRequest);
    RoomResponse deleteRoom(Long id);
}
