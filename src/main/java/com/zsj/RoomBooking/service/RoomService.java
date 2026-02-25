package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.RoomRequest;
import com.zsj.RoomBooking.model.RoomResponse;
import com.zsj.RoomBooking.model.SearchRoomRequest;

import java.util.List;

public interface RoomService {
    List<RoomResponse> searchRooms(SearchRoomRequest searchRoomRequest);
    RoomResponse addRoom(RoomRequest roomRequest);
    RoomResponse deleteRoom(Long id);
}